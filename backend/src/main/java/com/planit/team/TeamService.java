package com.planit.team;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.planit.common.ApiException;
import com.planit.notification.NotificationService;
import com.planit.team.TeamDtos.MemberResponse;
import com.planit.team.TeamDtos.TeamResponse;
import com.planit.user.User;
import com.planit.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

	private final TeamRepository teams;
	private final TeamMemberRepository members;
	private final UserRepository users;
	private final NotificationService notifications;

	public TeamService(TeamRepository teams, TeamMemberRepository members, UserRepository users,
			NotificationService notifications) {
		this.teams = teams;
		this.members = members;
		this.users = users;
		this.notifications = notifications;
	}

	@Transactional
	public TeamResponse create(Long userId, String name) {
		Team team = teams.save(new Team(name.trim(), userId));
		TeamMember owner = members.save(new TeamMember(team.getId(), userId, TeamMember.Role.OWNER));
		return new TeamResponse(team.getId(), team.getName(), owner.getRole(), team.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> listMine(Long userId) {
		List<TeamMember> memberships = members.findByIdUserId(userId);
		Map<Long, Team> teamById = teams.findAllById(memberships.stream().map(TeamMember::getTeamId).toList())
				.stream().collect(Collectors.toMap(Team::getId, Function.identity()));
		return memberships.stream()
				.map(m -> {
					Team team = teamById.get(m.getTeamId());
					return new TeamResponse(team.getId(), team.getName(), m.getRole(), team.getCreatedAt());
				})
				.toList();
	}

	@Transactional
	public MemberResponse addMember(Long userId, Long teamId, String email) {
		TeamMember requester = requireMember(teamId, userId);
		if (requester.getRole() != TeamMember.Role.OWNER) {
			throw ApiException.forbidden("NOT_TEAM_OWNER", "팀장만 팀원을 추가할 수 있습니다.");
		}
		User user = users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
				.orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "해당 이메일로 가입한 사용자가 없습니다."));
		if (members.isMember(teamId, user.getId())) {
			throw ApiException.conflict("ALREADY_MEMBER", "이미 팀에 속한 사용자입니다.");
		}
		TeamMember member = members.save(new TeamMember(teamId, user.getId(), TeamMember.Role.MEMBER));
		return new MemberResponse(user.getId(), user.getEmail(), user.getName(), member.getRole(), member.getJoinedAt());
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> listMembers(Long userId, Long teamId) {
		requireMember(teamId, userId);
		List<TeamMember> memberships = members.findByIdTeamIdOrderByJoinedAt(teamId);
		Map<Long, User> userById = users.findAllById(memberships.stream().map(TeamMember::getUserId).toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return memberships.stream()
				.map(m -> {
					User user = userById.get(m.getUserId());
					return new MemberResponse(user.getId(), user.getEmail(), user.getName(), m.getRole(), m.getJoinedAt());
				})
				.toList();
	}

	/**
	 * 입력값: 요청한 사용자, 팀 id, 내보낼 사용자 id
	 * 출력값: 없음
	 * 기능: 본인이면 팀을 떠나고, 다른 사람이면 팀장만 내보낼 수 있다. 팀장은 떠나거나 내보낼 수 없다.
	 * 떠난 사람에게 남아 있던 그 팀 일정의 대기 알림은 취소한다.
	 */
	@Transactional
	public void removeMember(Long requesterId, Long teamId, Long targetUserId) {
		TeamMember requester = requireMember(teamId, requesterId);
		if (!requesterId.equals(targetUserId) && requester.getRole() != TeamMember.Role.OWNER) {
			throw ApiException.forbidden("NOT_TEAM_OWNER", "팀장만 다른 팀원을 내보낼 수 있습니다.");
		}
		TeamMember target = members.findMembership(teamId, targetUserId)
				.orElseThrow(() -> ApiException.notFound("MEMBER_NOT_FOUND", "이 팀의 구성원이 아닙니다."));
		if (target.getRole() == TeamMember.Role.OWNER) {
			throw ApiException.conflict("OWNER_CANNOT_LEAVE", "팀장은 팀을 떠나거나 내보낼 수 없습니다.");
		}
		members.delete(target);
		notifications.memberLeft(teamId, targetUserId);
	}

	/** 모든 팀 API는 이 검사를 먼저 통과해야 한다. */
	public TeamMember requireMember(Long teamId, Long userId) {
		return members.findMembership(teamId, userId)
				.orElseThrow(() -> ApiException.forbidden("NOT_TEAM_MEMBER", "이 팀의 구성원이 아닙니다."));
	}

}
