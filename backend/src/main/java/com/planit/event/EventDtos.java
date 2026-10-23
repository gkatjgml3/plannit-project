package com.planit.event;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class EventDtos {

	private EventDtos() {
	}

	/** 생성·수정에 함께 쓴다. version은 수정할 때만 필요하다. */
	public record EventRequest(
			@NotBlank @Size(max = 200) String title,
			@NotNull OffsetDateTime startsAt,
			@NotNull OffsetDateTime endsAt,
			@NotBlank @Size(max = 64) String timezone,
			@Size(max = 2000) String description,
			@Size(max = 255) String location,
			Long version) {
	}

	public record SettingsRequest(
			boolean hidden,
			OffsetDateTime reminderAt,
			@Size(max = 2000) String personalMemo) {
	}

	/** startsAt·endsAt은 일정에 저장된 timezone 기준 시각으로 돌려준다. */
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
			String personalMemo) {

		static EventResponse of(Event event, EventUserSetting setting) {
			ZoneId zone = ZoneId.of(event.getTimezone());
			return new EventResponse(
					event.getId(),
					event.getTeamId(),
					event.getCreatedBy(),
					event.getTitle(),
					event.getStartsAt().atZone(zone).toOffsetDateTime(),
					event.getEndsAt().atZone(zone).toOffsetDateTime(),
					event.getTimezone(),
					event.getDescription(),
					event.getLocation(),
					event.getVersion(),
					event.getCreatedAt(),
					event.getUpdatedAt(),
					setting != null && setting.isHidden(),
					setting == null ? null : setting.getReminderAt(),
					setting == null ? null : setting.getPersonalMemo());
		}

	}

	/** created가 false면 같은 Idempotency-Key로 이미 만든 일정을 돌려준 것. */
	public record CreateResult(EventResponse event, boolean created) {
	}

}
