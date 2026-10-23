package com.planit.event;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import com.planit.common.ApiExamples;
import com.planit.common.CurrentUser;
import com.planit.event.EventDtos.CreateResult;
import com.planit.event.EventDtos.EventRequest;
import com.planit.event.EventDtos.EventResponse;
import com.planit.event.EventDtos.SettingsRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
@Tag(name = "일정", description = "개인 일정과 캘린더 조회. 팀 일정 만들기는 팀 API에 있다.")
public class EventController {

	private static final String EVENT_ERRORS = "오류: 400 VALIDATION_FAILED(제목 등), 400 INVALID_REQUEST_BODY(없는 날짜 등), "
			+ "400 INVALID_TIME_RANGE, 400 INVALID_TIMEZONE, 400 INVALID_RECURRENCE";

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	/** 입력값: 일정 값, 선택 Idempotency-Key / 출력값: 201(새로 만듦) 또는 200(같은 요청 재시도) / 기능: 개인 일정 생성. */
	@PostMapping
	@Operation(summary = "개인 일정 만들기", description = "checklist는 준비물 목록(최대 50개), recurrence는 반복 규칙"
			+ "(DAILY·WEEKLY·MONTHLY, interval, until 또는 count, WEEKLY의 byDays). 반복 회차는 저장하지 않고 조회할 때 펼친다.\n\n"
			+ "Idempotency-Key를 보내면 같은 키·같은 내용의 재시도는 처음 만든 일정을 200으로 돌려주고, "
			+ "같은 키를 다른 내용에 쓰면 409 IDEMPOTENCY_KEY_REUSED.\n\n" + EVENT_ERRORS
			+ ", 400 INVALID_IDEMPOTENCY_KEY, 409 IDEMPOTENCY_KEY_REUSED")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT_REQUEST)))
	@ApiResponse(responseCode = "201", content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT)))
	@ApiResponse(responseCode = "200", description = "같은 Idempotency-Key·같은 내용으로 이미 만든 일정")
	@ApiResponse(responseCode = "400", content = @Content(examples = @ExampleObject(value = """
			{"code": "INVALID_RECURRENCE", "message": "until과 count는 함께 쓸 수 없습니다."}""")))
	@ApiResponse(responseCode = "409", description = "IDEMPOTENCY_KEY_REUSED", content = @Content(examples = @ExampleObject(value = ApiExamples.ERROR)))
	public ResponseEntity<EventResponse> create(@AuthenticationPrincipal Jwt jwt,
			@Parameter(description = "같은 요청의 중복 저장을 막는 키(100자 이하)")
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody EventRequest request) {
		return toCreatedResponse(eventService.create(CurrentUser.id(jwt), null, request, idempotencyKey));
	}

	/** 내 개인 일정과 내가 속한 팀의 일정을 함께 돌려준다. from·to는 timezone 기준 날짜(양 끝 포함). */
	@GetMapping
	@Operation(summary = "캘린더 조회", description = "내 개인 일정과 내가 속한 팀 일정을 함께 돌려준다(숨긴 일정 제외). "
			+ "반복 일정은 기간 안의 회차마다 한 항목이 되며 id는 원본 일정 id, occurrenceStartsAt은 회차 시작 시각이다. "
			+ "회차 시각은 일정의 timezone 현지 시각을 유지한다. 최대 366일.\n\n"
			+ "오류: 400 INVALID_DATE_RANGE, 400 DATE_RANGE_TOO_LONG, 400 INVALID_TIMEZONE, 400 INVALID_PARAMETER")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.CALENDAR)))
	public List<EventResponse> list(@AuthenticationPrincipal Jwt jwt,
			@Parameter(example = "2026-11-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@Parameter(example = "2026-11-30") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@Parameter(description = "from·to를 해석할 시간대") @RequestParam(defaultValue = "Asia/Seoul") String timezone) {
		return eventService.listCalendar(CurrentUser.id(jwt), from, to, timezone);
	}

	@GetMapping("/{id}")
	@Operation(summary = "일정 하나 조회", description = "반복 일정이면 원본(첫 회차) 값을 돌려준다. "
			+ "볼 권한이 없는 일정은 존재 여부를 숨기려고 404.\n\n오류: 404 EVENT_NOT_FOUND")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT)))
	@ApiResponse(responseCode = "404", description = "EVENT_NOT_FOUND", content = @Content(examples = @ExampleObject(value = ApiExamples.ERROR)))
	public EventResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return eventService.get(CurrentUser.id(jwt), id);
	}

	/**
	 * 입력값: 일정 id, 새 값(version 필수) / 출력값: 수정한 일정
	 * 기능: version이 다르거나 같은 version으로 동시에 수정되면 409 EVENT_VERSION_CONFLICT와 현재 값을 돌려준다.
	 */
	@PutMapping("/{id}")
	@Operation(summary = "일정 수정", description = "전체 교체. checklist를 빼면 준비물이 지워지고 recurrence를 빼면 반복하지 않는다. "
			+ "개인 일정은 작성자만, 팀 일정은 작성자나 팀장만 고칠 수 있다. 시작 시각이 바뀌면 알림 시각도 같은 만큼 옮긴다. "
			+ "\"이 회차만 수정\"은 아직 없다.\n\n" + EVENT_ERRORS
			+ ", 400 VERSION_REQUIRED, 403 NOT_EVENT_EDITOR, 404 EVENT_NOT_FOUND, 409 EVENT_VERSION_CONFLICT(current 포함)")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT_UPDATE_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT)))
	@ApiResponse(responseCode = "409", description = "EVENT_VERSION_CONFLICT", content = @Content(examples = @ExampleObject(value = ApiExamples.VERSION_CONFLICT)))
	public EventResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody EventRequest request) {
		Long userId = CurrentUser.id(jwt);
		try {
			return eventService.update(userId, id, request);
		}
		catch (ObjectOptimisticLockingFailureException e) {
			// 수정 트랜잭션은 이미 롤백되었으므로 새로 읽은 현재 값을 돌려준다.
			throw new EventVersionConflictException(eventService.get(userId, id));
		}
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "일정 삭제", description = "반복 일정이면 모든 회차가 지워진다. 일정 알림은 취소되고, 팀 일정이면 다른 팀원에게 알린다.\n\n"
			+ "오류: 403 NOT_EVENT_EDITOR, 404 EVENT_NOT_FOUND")
	@ApiResponse(responseCode = "204", description = "삭제함")
	@ApiResponse(responseCode = "403", content = @Content(examples = @ExampleObject(value = """
			{"code": "NOT_EVENT_EDITOR", "message": "이 일정을 수정할 권한이 없습니다."}""")))
	@ApiResponse(responseCode = "404", content = @Content(examples = @ExampleObject(value = """
			{"code": "EVENT_NOT_FOUND", "message": "일정을 찾을 수 없습니다."}""")))
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		eventService.delete(CurrentUser.id(jwt), id);
	}

	/** 숨김·알림 시각·개인 메모처럼 나에게만 적용되는 설정. */
	@PutMapping("/{id}/settings")
	@Operation(summary = "내 일정 설정", description = "숨김·알림 시각·개인 메모는 나에게만 적용된다(팀 일정도 복사하지 않음). "
			+ "reminderAt을 정하면 알림이 만들어지고, 바꾸면 갱신, null이면 취소된다.\n\n오류: 404 EVENT_NOT_FOUND")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.SETTINGS_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT)))
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
