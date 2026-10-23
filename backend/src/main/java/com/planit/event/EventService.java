package com.planit.event;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.planit.common.ApiException;
import com.planit.event.EventDtos.CreateResult;
import com.planit.event.EventDtos.EventRequest;
import com.planit.event.EventDtos.EventResponse;
import com.planit.event.EventDtos.RecurrenceRequest;
import com.planit.event.EventDtos.SettingsRequest;
import com.planit.notification.Notification;
import com.planit.notification.NotificationService;
import com.planit.team.TeamMember;
import com.planit.team.TeamMemberRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class EventService {

	private static final int MAX_REQUEST_ID_LENGTH = 100;
	/** 캘린더 한 번에 조회할 수 있는 최대 기간(일). 반복 일정을 끝없이 펼치지 않게 한다. */
	static final int MAX_RANGE_DAYS = 366;

	private final EventRepository events;
	private final EventUserSettingRepository settings;
	private final EventChecklistItemRepository checklistItems;
	private final TeamMemberRepository members;
	private final NotificationService notifications;
	private final TransactionTemplate tx;

	public EventService(EventRepository events, EventUserSettingRepository settings,
			EventChecklistItemRepository checklistItems, TeamMemberRepository members,
			NotificationService notifications, PlatformTransactionManager transactionManager) {
		this.events = events;
		this.settings = settings;
		this.checklistItems = checklistItems;
		this.members = members;
		this.notifications = notifications;
		this.tx = new TransactionTemplate(transactionManager);
	}

	/** 검증을 마친 새 일정 값. 분석 확정처럼 다른 기능에서 일정을 만들 때도 쓴다. */
	public record NewEventData(String title, Instant startsAt, Instant endsAt, String timezone, String description,
			String location, List<String> checklist, RecurrenceRule recurrence) {
	}

	/**
	 * 입력값: 사용자, 팀 id(개인 일정이면 null), 요청, Idempotency-Key
	 * 출력값: 만든 일정(또는 같은 키·같은 내용으로 이미 만든 일정)
	 * 기능: 팀 일정의 팀원 여부는 호출하는 쪽(TeamController)에서 확인한다.
	 * 같은 키를 다른 내용에 재사용하면 409로 거절한다. 일정과 준비물은 한 트랜잭션에서 저장하지만,
	 * 중복 키 충돌을 잡아야 하므로 이 메서드 전체를 하나의 트랜잭션으로 묶지 않는다.
	 */
	public CreateResult create(Long userId, Long teamId, EventRequest request, String idempotencyKey) {
		NewEventData data = validate(request);
		String requestId = normalizeRequestId(idempotencyKey);
		String fingerprint = RequestFingerprint.of(teamId, data.title(), data.startsAt(), data.endsAt(),
				data.timezone(), data.description(), data.location(), data.checklist(), data.recurrence());

		if (requestId != null) {
			Optional<Event> existing = events.findByCreatedByAndRequestId(userId, requestId);
			if (existing.isPresent()) {
				return replay(existing.get(), userId, fingerprint);
			}
		}

		try {
			EventResponse created = tx.execute(status -> {
				Event event = insert(userId, teamId, data, requestId, requestId == null ? null : fingerprint);
				return EventResponse.of(event, null, checklistItems.findByEventIdOrderBySortOrder(event.getId()));
			});
			return new CreateResult(created, true);
		}
		catch (DataIntegrityViolationException e) {
			// 같은 키의 요청이 동시에 들어와 다른 요청이 먼저 저장한 경우
			if (requestId == null) {
				throw e;
			}
			Event saved = events.findByCreatedByAndRequestId(userId, requestId).orElseThrow(() -> e);
			return replay(saved, userId, fingerprint);
		}
	}

	/**
	 * 입력값: 사용자, 팀 id(개인이면 null), 검증한 일정 값
	 * 출력값: 만든 일정 id
	 * 기능: 호출한 쪽의 트랜잭션 안에서 일정과 준비물을 저장한다. 분석 확정에서 쓴다.
	 */
	@Transactional
	public Long createWithinTransaction(Long userId, Long teamId, NewEventData data) {
		return insert(userId, teamId, data, null, null).getId();
	}

	private Event insert(Long userId, Long teamId, NewEventData data, String requestId, String fingerprint) {
		Event event = new Event(teamId, userId, requestId, fingerprint);
		event.update(data.title(), data.startsAt(), data.endsAt(), data.timezone(), data.description(),
				data.location());
		event.setRecurrence(data.recurrence());
		Event saved = events.saveAndFlush(event);
		saveChecklist(saved.getId(), data.checklist());
		if (teamId != null) {
			notifications.teamEventChanged(saved.getId(), teamId, userId, Notification.Kind.TEAM_EVENT_CREATED);
		}
		return saved;
	}

	/** 처음 요청과 내용이 같을 때만 저장된 일정을 돌려준다. */
	private CreateResult replay(Event existing, Long userId, String fingerprint) {
		String stored = existing.getRequestFingerprint() != null ? existing.getRequestFingerprint()
				: RequestFingerprint.of(existing, checklistContents(existing.getId()));
		if (!stored.equals(fingerprint)) {
			throw ApiException.conflict("IDEMPOTENCY_KEY_REUSED",
					"같은 Idempotency-Key가 다른 내용의 요청에 이미 사용되었습니다. 새 요청에는 새 키를 사용하세요.");
		}
		return new CreateResult(toResponse(existing, userId), false);
	}

	/**
	 * 입력값: 사용자, 조회 시작·끝 날짜(양 끝 포함), 날짜를 해석할 timezone
	 * 출력값: 내 개인 일정과 내가 속한 팀 일정. 반복 일정은 기간 안의 회차마다 한 항목으로 펼친다.
	 * 기능: 회차는 저장하지 않고 원본 일정 하나에서 조회할 때마다 계산한다. 최대 366일까지 조회할 수 있다.
	 */
	@Transactional(readOnly = true)
	public List<EventResponse> listCalendar(Long userId, LocalDate from, LocalDate to, String timezone) {
		if (to.isBefore(from)) {
			throw ApiException.badRequest("INVALID_DATE_RANGE", "to는 from과 같거나 뒤의 날짜여야 합니다.");
		}
		if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
			throw ApiException.badRequest("DATE_RANGE_TOO_LONG", "한 번에 " + MAX_RANGE_DAYS + "일까지만 조회할 수 있습니다.");
		}
		ZoneId zone = parseZone(timezone);
		Instant start = from.atStartOfDay(zone).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();

		List<Event> found = events.findCalendar(userId, start, end);
		if (found.isEmpty()) {
			return List.of();
		}
		List<Long> ids = found.stream().map(Event::getId).toList();
		Map<Long, EventUserSetting> settingByEvent = settings.findByIdUserIdAndIdEventIdIn(userId, ids)
				.stream().collect(Collectors.toMap(EventUserSetting::getEventId, Function.identity()));
		Map<Long, List<EventChecklistItem>> checklistByEvent = checklistItems
				.findByEventIdInOrderByEventIdAscSortOrderAsc(ids)
				.stream().collect(Collectors.groupingBy(EventChecklistItem::getEventId));

		List<EventResponse> result = new ArrayList<>();
		for (Event event : found) {
			EventUserSetting setting = settingByEvent.get(event.getId());
			List<EventChecklistItem> checklist = checklistByEvent.getOrDefault(event.getId(), List.of());
			RecurrenceRule rule = event.getRecurrence();
			if (rule == null) {
				result.add(EventResponse.of(event, setting, checklist));
				continue;
			}
			for (RecurrenceRule.Occurrence occurrence : rule.expand(event.getStartsAt(), event.getEndsAt(),
					ZoneId.of(event.getTimezone()), start, end)) {
				result.add(EventResponse.ofOccurrence(event, setting, checklist, occurrence));
			}
		}
		result.sort(Comparator.comparing((EventResponse r) -> r.startsAt().toInstant()).thenComparing(EventResponse::id));
		return result;
	}

	@Transactional(readOnly = true)
	public EventResponse get(Long userId, Long eventId) {
		return toResponse(findVisible(userId, eventId), userId);
	}

	/**
	 * 입력값: 사용자, 일정 id, 새 값(version 포함)
	 * 출력값: 수정한 일정
	 * 기능: 전체 교체 방식으로 일정·준비물·반복 규칙을 바꾼다. version이 다르면 EventVersionConflictException.
	 * 같은 version으로 동시에 들어온 요청은 DB의 낙관적 잠금(@Version)이 막고, 컨트롤러가 현재 값을 붙여 409로 바꾼다.
	 */
	@Transactional
	public EventResponse update(Long userId, Long eventId, EventRequest request) {
		Event event = findVisible(userId, eventId);
		requireEditable(userId, event);
		NewEventData data = validate(request);
		if (request.version() == null) {
			throw ApiException.badRequest("VERSION_REQUIRED", "수정할 때는 version이 필요합니다.");
		}
		if (!request.version().equals(event.getVersion())) {
			throw new EventVersionConflictException(toResponse(event, userId));
		}
		Instant previousStart = event.getStartsAt();
		event.update(data.title(), data.startsAt(), data.endsAt(), data.timezone(), data.description(),
				data.location());
		event.setRecurrence(data.recurrence());
		event.touch();
		Event saved = events.saveAndFlush(event);
		checklistItems.deleteByEventId(eventId);
		saveChecklist(eventId, data.checklist());
		if (!previousStart.equals(saved.getStartsAt())) {
			moveReminders(eventId, Duration.between(previousStart, saved.getStartsAt()));
		}
		if (saved.isTeamEvent()) {
			notifications.teamEventChanged(eventId, saved.getTeamId(), userId, Notification.Kind.TEAM_EVENT_UPDATED);
		}
		return toResponse(saved, userId);
	}

	/**
	 * 입력값: 일정 id, 시작 시각이 움직인 만큼의 시간
	 * 출력값: 없음
	 * 기능: 일정 시간이 바뀌면 사용자들이 정한 알림 시각도 같은 만큼 옮겨 일정과 알림의 간격을 유지한다.
	 */
	private void moveReminders(Long eventId, Duration shift) {
		for (EventUserSetting setting : settings.findByIdEventIdAndReminderAtIsNotNull(eventId)) {
			Instant moved = setting.getReminderAt().plus(shift);
			setting.moveReminder(moved);
			notifications.reminderChanged(eventId, setting.getUserId(), moved);
		}
	}

	/** 입력값: 사용자, 일정 id / 출력값: 없음 / 기능: 일정을 지우고 일정 알림을 취소하며, 팀 일정이면 팀원에게 알린다. */
	@Transactional
	public void delete(Long userId, Long eventId) {
		Event event = findVisible(userId, eventId);
		requireEditable(userId, event);
		event.markDeleted();
		notifications.cancelReminders(eventId);
		if (event.isTeamEvent()) {
			notifications.teamEventChanged(eventId, event.getTeamId(), userId, Notification.Kind.TEAM_EVENT_DELETED);
		}
	}

	/**
	 * 입력값: 사용자, 일정 id, 숨김·알림 시각·메모
	 * 출력값: 설정을 반영한 일정
	 * 기능: 알림 시각이 생기거나 바뀌거나 지워지면 알림을 만들거나 바꾸거나 취소한다.
	 */
	@Transactional
	public EventResponse updateSettings(Long userId, Long eventId, SettingsRequest request) {
		Event event = findVisible(userId, eventId);
		EventUserSetting setting = settings.findSetting(eventId, userId)
				.orElseGet(() -> new EventUserSetting(eventId, userId));
		Instant previousReminder = setting.getReminderAt();
		Instant reminderAt = request.reminderAt() == null ? null : request.reminderAt().toInstant();
		setting.update(request.hidden(), reminderAt, request.personalMemo());
		if (!Objects.equals(previousReminder, reminderAt)) {
			notifications.reminderChanged(eventId, userId, reminderAt);
		}
		return EventResponse.of(event, settings.save(setting), checklistItems.findByEventIdOrderBySortOrder(eventId));
	}

	/** 볼 수 없는 일정은 존재 여부도 알려주지 않도록 404로 응답한다. */
	private Event findVisible(Long userId, Long eventId) {
		return events.findByIdAndDeletedAtIsNull(eventId)
				.filter(e -> e.isTeamEvent() ? members.isMember(e.getTeamId(), userId)
						: e.getCreatedBy().equals(userId))
				.orElseThrow(() -> ApiException.notFound("EVENT_NOT_FOUND", "일정을 찾을 수 없습니다."));
	}

	/** 개인 일정은 작성자만, 팀 일정은 작성자 또는 팀장만 수정·삭제할 수 있다. */
	private void requireEditable(Long userId, Event event) {
		if (event.getCreatedBy().equals(userId)) {
			return;
		}
		boolean owner = event.isTeamEvent() && members.findMembership(event.getTeamId(), userId)
				.map(m -> m.getRole() == TeamMember.Role.OWNER)
				.orElse(false);
		if (!owner) {
			throw ApiException.forbidden("NOT_EVENT_EDITOR", "이 일정을 수정할 권한이 없습니다.");
		}
	}

	private EventResponse toResponse(Event event, Long userId) {
		return EventResponse.of(event, settings.findSetting(event.getId(), userId).orElse(null),
				checklistItems.findByEventIdOrderBySortOrder(event.getId()));
	}

	private void saveChecklist(Long eventId, List<String> checklist) {
		for (int i = 0; i < checklist.size(); i++) {
			checklistItems.save(new EventChecklistItem(eventId, checklist.get(i), i));
		}
	}

	private List<String> checklistContents(Long eventId) {
		return checklistItems.findByEventIdOrderBySortOrder(eventId).stream().map(EventChecklistItem::getContent)
				.toList();
	}

	/** 입력값: 요청 / 출력값: 검증한 일정 값 / 기능: 시간대·시작<종료·반복 규칙을 검사하고 준비물 공백을 정리한다. */
	private NewEventData validate(EventRequest request) {
		ZoneId zone = parseZone(request.timezone());
		Instant startsAt = request.startsAt().toInstant();
		Instant endsAt = request.endsAt().toInstant();
		if (!endsAt.isAfter(startsAt)) {
			throw ApiException.badRequest("INVALID_TIME_RANGE", "종료 시간은 시작 시간보다 늦어야 합니다.");
		}
		List<String> checklist = request.checklist() == null ? List.of()
				: request.checklist().stream().map(String::trim).toList();
		RecurrenceRule recurrence = parseRecurrence(request.recurrence(), startsAt.atZone(zone).toLocalDate());
		return new NewEventData(request.title().trim(), startsAt, endsAt, request.timezone(), request.description(),
				request.location(), checklist, recurrence);
	}

	private static RecurrenceRule parseRecurrence(RecurrenceRequest request, LocalDate startDate) {
		if (request == null) {
			return null;
		}
		try {
			return RecurrenceRule.parse(request.frequency(), request.interval(), request.until(), request.count(),
					request.byDays(), startDate);
		}
		catch (RecurrenceRule.InvalidRecurrenceException e) {
			throw ApiException.badRequest("INVALID_RECURRENCE", e.getMessage());
		}
	}

	private static ZoneId parseZone(String timezone) {
		try {
			return ZoneId.of(Objects.requireNonNull(timezone));
		}
		catch (DateTimeException | NullPointerException e) {
			throw ApiException.badRequest("INVALID_TIMEZONE", "알 수 없는 timezone입니다: " + timezone);
		}
	}

	private static String normalizeRequestId(String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return null;
		}
		String trimmed = idempotencyKey.trim();
		if (trimmed.length() > MAX_REQUEST_ID_LENGTH) {
			throw ApiException.badRequest("INVALID_IDEMPOTENCY_KEY", "Idempotency-Key는 100자 이하여야 합니다.");
		}
		return trimmed;
	}

}
