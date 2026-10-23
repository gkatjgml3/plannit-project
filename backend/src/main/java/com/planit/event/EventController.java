package com.planit.event;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import com.planit.common.CurrentUser;
import com.planit.event.EventDtos.CreateResult;
import com.planit.event.EventDtos.EventRequest;
import com.planit.event.EventDtos.EventResponse;
import com.planit.event.EventDtos.SettingsRequest;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping
	public ResponseEntity<EventResponse> create(@AuthenticationPrincipal Jwt jwt,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody EventRequest request) {
		return toCreatedResponse(eventService.create(CurrentUser.id(jwt), null, request, idempotencyKey));
	}

	/** 내 개인 일정과 내가 속한 팀의 일정을 함께 돌려준다. from·to는 timezone 기준 날짜(양 끝 포함). */
	@GetMapping
	public List<EventResponse> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "Asia/Seoul") String timezone) {
		return eventService.listCalendar(CurrentUser.id(jwt), from, to, timezone);
	}

	@GetMapping("/{id}")
	public EventResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return eventService.get(CurrentUser.id(jwt), id);
	}

	@PutMapping("/{id}")
	public EventResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody EventRequest request) {
		return eventService.update(CurrentUser.id(jwt), id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		eventService.delete(CurrentUser.id(jwt), id);
	}

	/** 숨김·알림 시각·개인 메모처럼 나에게만 적용되는 설정. */
	@PutMapping("/{id}/settings")
	public EventResponse updateSettings(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody SettingsRequest request) {
		return eventService.updateSettings(CurrentUser.id(jwt), id, request);
	}

	/** 새로 만들었으면 201, 같은 Idempotency-Key로 이미 만든 일정이면 200. */
	public static ResponseEntity<EventResponse> toCreatedResponse(CreateResult result) {
		if (!result.created()) {
			return ResponseEntity.ok(result.event());
		}
		return ResponseEntity.created(URI.create("/api/events/" + result.event().id())).body(result.event());
	}

}
