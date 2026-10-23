package com.planit.team;

import java.util.List;

import com.planit.common.ApiExamples;
import com.planit.common.CurrentUser;
import com.planit.event.EventController;
import com.planit.event.EventDtos.EventRequest;
import com.planit.event.EventDtos.EventResponse;
import com.planit.event.EventService;
import com.planit.team.TeamDtos.AddMemberRequest;
import com.planit.team.TeamDtos.MemberResponse;
import com.planit.team.TeamDtos.TeamRequest;
import com.planit.team.TeamDtos.TeamResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "팀", description = "반·동아리·스터디 같은 팀과 팀원, 팀 일정. 모든 팀 API는 요청자가 그 팀의 구성원인지 확인한다.")
public class TeamController {

	private static final String NOT_MEMBER = "403 NOT_TEAM_MEMBER(팀원이 아님)";

	private final TeamService teamService;
	private final EventService eventService;

	public TeamController(TeamService teamService, EventService eventService) {
		this.teamService = teamService;
		this.eventService = eventService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "팀 만들기", description = "만든 사람이 팀장(OWNER)이 된다.\n\n오류: 400 VALIDATION_FAILED")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.TEAM_REQUEST)))
	@ApiResponse(responseCode = "201", content = @Content(examples = @ExampleObject(value = ApiExamples.TEAM)))
	public TeamResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TeamRequest request) {
		return teamService.create(CurrentUser.id(jwt), request.name());
	}

	@GetMapping
	@Operation(summary = "내가 속한 팀 목록")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = "[" + ApiExamples.TEAM + "]")))
	public List<TeamResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
		return teamService.listMine(CurrentUser.id(jwt));
	}

	@PostMapping("/{teamId}/members")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "팀원 추가", description = "팀장만 가입한 사용자를 이메일로 추가할 수 있다.\n\n오류: " + NOT_MEMBER
			+ ", 403 NOT_TEAM_OWNER, 404 USER_NOT_FOUND, 409 ALREADY_MEMBER")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.MEMBER_REQUEST)))
	@ApiResponse(responseCode = "201", content = @Content(examples = @ExampleObject(value = ApiExamples.MEMBER)))
	@ApiResponse(responseCode = "403", content = @Content(examples = @ExampleObject(value = """
			{"code": "NOT_TEAM_OWNER", "message": "팀장만 팀원을 추가할 수 있습니다."}""")))
	public MemberResponse addMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId,
			@Valid @RequestBody AddMemberRequest request) {
		return teamService.addMember(CurrentUser.id(jwt), teamId, request.email());
	}

	@GetMapping("/{teamId}/members")
	@Operation(summary = "팀원 목록", description = "오류: " + NOT_MEMBER)
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = "[" + ApiExamples.MEMBER + "]")))
	public List<MemberResponse> listMembers(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
		return teamService.listMembers(CurrentUser.id(jwt), teamId);
	}

	/** 입력값: 팀 id, 사용자 id / 출력값: 204 / 기능: 본인 탈퇴 또는 팀장이 팀원을 내보낸다. */
	@DeleteMapping("/{teamId}/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "팀 탈퇴·내보내기", description = "본인이면 탈퇴, 다른 사람이면 팀장만 내보낼 수 있다. 팀장은 떠날 수 없다. "
			+ "떠난 사람에게 남은 그 팀 일정의 대기 알림은 취소된다.\n\n오류: " + NOT_MEMBER
			+ ", 403 NOT_TEAM_OWNER, 404 MEMBER_NOT_FOUND, 409 OWNER_CANNOT_LEAVE")
	@ApiResponse(responseCode = "204", description = "팀에서 빠짐")
	@ApiResponse(responseCode = "409", content = @Content(examples = @ExampleObject(value = """
			{"code": "OWNER_CANNOT_LEAVE", "message": "팀장은 팀을 떠나거나 내보낼 수 없습니다."}""")))
	public void removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId, @PathVariable Long userId) {
		teamService.removeMember(CurrentUser.id(jwt), teamId, userId);
	}

	@PostMapping("/{teamId}/events")
	@Operation(summary = "팀 일정 만들기", description = "팀원이면 누구나 만들 수 있다. 원본 하나만 저장되고 팀원 캘린더에 함께 보인다. "
			+ "다른 팀원에게 변경 알림이 만들어진다. 요청 형식과 Idempotency-Key 규칙은 개인 일정 만들기와 같다.\n\n오류: "
			+ NOT_MEMBER + ", 400 VALIDATION_FAILED, 400 INVALID_TIME_RANGE, 400 INVALID_RECURRENCE, 409 IDEMPOTENCY_KEY_REUSED")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT_REQUEST)))
	@ApiResponse(responseCode = "201", content = @Content(examples = @ExampleObject(value = ApiExamples.EVENT)))
	@ApiResponse(responseCode = "200", description = "같은 Idempotency-Key·같은 내용으로 이미 만든 일정")
	public ResponseEntity<EventResponse> createEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId,
			@Parameter(description = "같은 요청의 중복 저장을 막는 키(100자 이하)")
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody EventRequest request) {
		Long userId = CurrentUser.id(jwt);
		teamService.requireMember(teamId, userId);
		return EventController.toCreatedResponse(eventService.create(userId, teamId, request, idempotencyKey));
	}

}
