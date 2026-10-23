package com.planit.team;

import java.util.List;

import com.planit.common.CurrentUser;
import com.planit.event.EventController;
import com.planit.event.EventDtos.EventRequest;
import com.planit.event.EventDtos.EventResponse;
import com.planit.event.EventService;
import com.planit.team.TeamDtos.AddMemberRequest;
import com.planit.team.TeamDtos.MemberResponse;
import com.planit.team.TeamDtos.TeamRequest;
import com.planit.team.TeamDtos.TeamResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
public class TeamController {

	private final TeamService teamService;
	private final EventService eventService;

	public TeamController(TeamService teamService, EventService eventService) {
		this.teamService = teamService;
		this.eventService = eventService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TeamResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TeamRequest request) {
		return teamService.create(CurrentUser.id(jwt), request.name());
	}

	@GetMapping
	public List<TeamResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
		return teamService.listMine(CurrentUser.id(jwt));
	}

	@PostMapping("/{teamId}/members")
	@ResponseStatus(HttpStatus.CREATED)
	public MemberResponse addMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId,
			@Valid @RequestBody AddMemberRequest request) {
		return teamService.addMember(CurrentUser.id(jwt), teamId, request.email());
	}

	@GetMapping("/{teamId}/members")
	public List<MemberResponse> listMembers(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
		return teamService.listMembers(CurrentUser.id(jwt), teamId);
	}

	@PostMapping("/{teamId}/events")
	public ResponseEntity<EventResponse> createEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody EventRequest request) {
		Long userId = CurrentUser.id(jwt);
		teamService.requireMember(teamId, userId);
		return EventController.toCreatedResponse(eventService.create(userId, teamId, request, idempotencyKey));
	}

}
