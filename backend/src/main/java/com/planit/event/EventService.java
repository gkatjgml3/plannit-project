package com.planit.event;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import com.planit.event.EventDtos.SettingsRequest;
import com.planit.team.TeamMember;
import com.planit.team.TeamMemberRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

	private static final int MAX_REQUEST_ID_LENGTH = 100;

	private final EventRepository events;
	private final EventUserSettingRepository settings;
	private final TeamMemberRepository members;

	public EventService(EventRepository events, EventUserSettingRepository settings, TeamMemberRepository members) {
		this.events = events;
		this.settings = settings;
		this.members = members;
	}

	/**
	 * teamId가 null이면 개인 일정을 만든다. 팀 일정의 팀원 여부는 호출하는 쪽(TeamController)에서 확인한다.
	 * 같은 Idempotency-Key와 같은 내용으로 다시 요청하면 새로 만들지 않고 처음 만든 일정을 돌려준다.
	 * 같은 키를 다른 내용에 재사용하면 409로 거절한다.
	 * 중복 키 충돌을 잡아야 하므로 이 메서드 전체를 하나의 트랜잭션으로 묶지 않는다.
	 */
	public CreateResult create(Long userId, Long teamId, EventRequest request, String idempotencyKey) {
		ValidTimes times = validate(request);
		String requestId = normalizeRequestId(idempotencyKey);
		String title = request.title().trim();
		String fingerprint = RequestFingerprint.of(teamId, title, times.startsAt(), times.endsAt(),
				request.timezone(), request.description(), request.location());

		if (requestId != null) {
			Optional<Event> existing = events.findByCreatedByAndRequestId(userId, requestId);
			if (existing.isPresent()) {
				return replay(existing.get(), userId, fingerprint);
			}
		}

		Event event = new Event(teamId, userId, requestId, requestId == null ? null : fingerprint);
		event.update(title, times.startsAt(), times.endsAt(), request.timezone(),
				request.description(), request.location());
		try {
			return new CreateResult(EventResponse.of(events.saveAndFlush(event), null), true);
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

	/** 처음 요청과 내용이 같을 때만 저장된 일정을 돌려준다. */
	private CreateResult replay(Event existing, Long userId, String fingerprint) {
		String stored = existing.getRequestFingerprint() != null ? existing.getRequestFingerprint()
				: RequestFingerprint.of(existing);
		if (!stored.equals(fingerprint)) {
			throw ApiException.conflict("IDEMPOTENCY_KEY_REUSED",
					"같은 Idempotency-Key가 다른 내용의 요청에 이미 사용되었습니다. 새 요청에는 새 키를 사용하세요.");
		}
		return new CreateResult(toResponse(existing, userId), false);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> listCalendar(Long userId, LocalDate from, LocalDate to, String timezone) {
		if (to.isBefore(from)) {
			throw ApiException.badRequest("INVALID_DATE_RANGE", "to는 from과 같거나 뒤의 날짜여야 합니다.");
		}
		ZoneId zone = parseZone(timezone);
		Instant start = from.atStartOfDay(zone).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();

		List<Event> found = events.findCalendar(userId, start, end);
		if (found.isEmpty()) {
			return List.of();
		}
		Map<Long, EventUserSetting> settingByEvent = settings
				.findByIdUserIdAndIdEventIdIn(userId, found.stream().map(Event::getId).toList())
				.stream().collect(Collectors.toMap(EventUserSetting::getEventId, Function.identity()));
		return found.stream().map(e -> EventResponse.of(e, settingByEvent.get(e.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public EventResponse get(Long userId, Long eventId) {
		return toResponse(findVisible(userId, eventId), userId);
	}

	@Transactional
	public EventResponse update(Long userId, Long eventId, EventRequest request) {
		Event event = findVisible(userId, eventId);
		requireEditable(userId, event);
		ValidTimes times = validate(request);
		if (request.version() == null) {
			throw ApiException.badRequest("VERSION_REQUIRED", "수정할 때는 version이 필요합니다.");
		}
		if (!request.version().equals(event.getVersion())) {
			throw ApiException.conflict("VERSION_CONFLICT", "다른 곳에서 먼저 수정되었습니다. 새로 불러온 뒤 다시 시도하세요.");
		}
		event.update(request.title().trim(), times.startsAt(), times.endsAt(), request.timezone(),
				request.description(), request.location());
		return toResponse(events.saveAndFlush(event), userId);
	}

	@Transactional
	public void delete(Long userId, Long eventId) {
		Event event = findVisible(userId, eventId);
		requireEditable(userId, event);
		event.markDeleted();
	}

	@Transactional
	public EventResponse updateSettings(Long userId, Long eventId, SettingsRequest request) {
		Event event = findVisible(userId, eventId);
		EventUserSetting setting = settings.findSetting(eventId, userId)
				.orElseGet(() -> new EventUserSetting(eventId, userId));
		setting.update(request.hidden(), request.reminderAt() == null ? null : request.reminderAt().toInstant(),
				request.personalMemo());
		return EventResponse.of(event, settings.save(setting));
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
		return EventResponse.of(event, settings.findSetting(event.getId(), userId).orElse(null));
	}

	private ValidTimes validate(EventRequest request) {
		parseZone(request.timezone());
		Instant startsAt = request.startsAt().toInstant();
		Instant endsAt = request.endsAt().toInstant();
		if (!endsAt.isAfter(startsAt)) {
			throw ApiException.badRequest("INVALID_TIME_RANGE", "종료 시간은 시작 시간보다 늦어야 합니다.");
		}
		return new ValidTimes(startsAt, endsAt);
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

	private record ValidTimes(Instant startsAt, Instant endsAt) {
	}

}
