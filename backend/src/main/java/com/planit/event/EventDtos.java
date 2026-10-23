package com.planit.event;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class EventDtos {

	private EventDtos() {
	}

	/**
	 * 생성·수정에 함께 쓴다. version은 수정할 때만 필요하다.
	 * 수정은 전체 교체다. checklist를 비우거나 빼면 준비물이 모두 지워지고, recurrence를 빼면 반복하지 않는 일정이 된다.
	 */
	public record EventRequest(
			@NotBlank @Size(max = 200) String title,
			@NotNull OffsetDateTime startsAt,
			@NotNull OffsetDateTime endsAt,
			@NotBlank @Size(max = 64) String timezone,
			@Size(max = 2000) String description,
			@Size(max = 255) String location,
			Long version,
			@Size(max = 50) List<@NotBlank @Size(max = 200) String> checklist,
			@Valid RecurrenceRequest recurrence) {
	}

	/**
	 * 반복 규칙 요청. frequency: DAILY·WEEKLY·MONTHLY, interval: 기본 1, until(yyyy-MM-dd, 그날 포함)과 count는 둘 중 하나만,
	 * byDays: WEEKLY에서만 MO·TU·WE·TH·FR·SA·SU. 값 검사는 서버가 해서 틀리면 400 INVALID_RECURRENCE를 돌려준다.
	 */
	public record RecurrenceRequest(String frequency, Integer interval, String until, Integer count,
			List<String> byDays) {
	}

	public record SettingsRequest(
			boolean hidden,
			OffsetDateTime reminderAt,
			@Size(max = 2000) String personalMemo) {
	}

	public record ChecklistItemResponse(Long id, String content, int sortOrder) {

		static ChecklistItemResponse of(EventChecklistItem item) {
			return new ChecklistItemResponse(item.getId(), item.getContent(), item.getSortOrder());
		}

	}

	public record RecurrenceResponse(String frequency, int interval, String until, Integer count,
			List<String> byDays) {

		static RecurrenceResponse of(RecurrenceRule rule) {
			if (rule == null) {
				return null;
			}
			return new RecurrenceResponse(rule.frequency().name(), rule.interval(),
					rule.until() == null ? null : rule.until().toString(), rule.count(),
					rule.byDayCodes().isEmpty() ? null : rule.byDayCodes());
		}

	}

	/**
	 * startsAt·endsAt은 일정에 저장된 timezone 기준 시각으로 돌려준다.
	 * 캘린더 조회에서 반복 일정은 회차마다 한 항목이 되며, 이때 id는 원본 일정 id, startsAt·endsAt은 그 회차의 시각,
	 * occurrenceStartsAt은 회차 시작 시각이다. 반복하지 않는 일정과 단건 조회에서는 occurrenceStartsAt이 null이다.
	 */
	public record EventResponse(
			Long id,
			Long teamId,
			Long createdBy,
			String title,
			OffsetDateTime startsAt,
			OffsetDateTime endsAt,
			String timezone,
			String description,
			String location,
			Long version,
			Instant createdAt,
			Instant updatedAt,
			boolean hidden,
			Instant reminderAt,
			String personalMemo,
			List<ChecklistItemResponse> checklist,
			RecurrenceResponse recurrence,
			OffsetDateTime occurrenceStartsAt) {

		static EventResponse of(Event event, EventUserSetting setting, List<EventChecklistItem> checklist) {
			ZoneId zone = ZoneId.of(event.getTimezone());
			return build(event, setting, checklist, event.getStartsAt().atZone(zone).toOffsetDateTime(),
					event.getEndsAt().atZone(zone).toOffsetDateTime(), null);
		}

		static EventResponse ofOccurrence(Event event, EventUserSetting setting, List<EventChecklistItem> checklist,
				RecurrenceRule.Occurrence occurrence) {
			OffsetDateTime start = occurrence.start().toOffsetDateTime();
			return build(event, setting, checklist, start, occurrence.end().toOffsetDateTime(), start);
		}

		private static EventResponse build(Event event, EventUserSetting setting, List<EventChecklistItem> checklist,
				OffsetDateTime startsAt, OffsetDateTime endsAt, OffsetDateTime occurrenceStartsAt) {
			return new EventResponse(
					event.getId(),
					event.getTeamId(),
					event.getCreatedBy(),
					event.getTitle(),
					startsAt,
					endsAt,
					event.getTimezone(),
					event.getDescription(),
					event.getLocation(),
					event.getVersion(),
					event.getCreatedAt(),
					event.getUpdatedAt(),
					setting != null && setting.isHidden(),
					setting == null ? null : setting.getReminderAt(),
					setting == null ? null : setting.getPersonalMemo(),
					checklist.stream().map(ChecklistItemResponse::of).toList(),
					RecurrenceResponse.of(event.getRecurrence()),
					occurrenceStartsAt);
		}

	}

	/** created가 false면 같은 Idempotency-Key로 이미 만든 일정을 돌려준 것. */
	public record CreateResult(EventResponse event, boolean created) {
	}

}
