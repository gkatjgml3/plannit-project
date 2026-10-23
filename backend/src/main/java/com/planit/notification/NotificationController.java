package com.planit.notification;

import java.util.List;

import com.planit.common.ApiExamples;
import com.planit.common.CurrentUser;
import com.planit.notification.NotificationService.NotificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림", description = "일정 알림(EVENT_REMINDER)과 팀 일정 변경 알림(TEAM_EVENT_CREATED·UPDATED·DELETED). "
		+ "상태: PENDING → SENT, 실패가 이어지면 FAILED, 대상이 사라지면 CANCELED. 실제 푸시·이메일 발송은 아직 없다.")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	/** 입력값: 선택 status(PENDING·SENT·FAILED·CANCELED) / 출력값: 내 알림 최근 100개 / 기능: 기본은 취소된 알림을 뺀다. */
	@GetMapping
	@Operation(summary = "내 알림 목록", description = "최근 100개. 일정 제목 같은 내용은 없으니 eventId로 일정을 조회한다.\n\n"
			+ "오류: 400 INVALID_PARAMETER(알 수 없는 status)")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.NOTIFICATIONS)))
	public List<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) Notification.Status status) {
		return notificationService.list(CurrentUser.id(jwt), status);
	}

	/** 입력값: 알림 id / 출력값: 읽음 처리한 알림 / 기능: 다른 사람의 알림이면 404. */
	@PostMapping("/{id}/read")
	@Operation(summary = "알림 읽음 처리", description = "오류: 404 NOTIFICATION_NOT_FOUND(다른 사람의 알림 포함)")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.NOTIFICATION)))
	@ApiResponse(responseCode = "404", content = @Content(examples = @ExampleObject(value = """
			{"code": "NOTIFICATION_NOT_FOUND", "message": "알림을 찾을 수 없습니다."}""")))
	public NotificationResponse markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return notificationService.markRead(CurrentUser.id(jwt), id);
	}

}
