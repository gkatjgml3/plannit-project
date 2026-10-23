package com.planit.team;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.planit.common.ApiException;
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

	public TeamService(TeamRepository teams, TeamMemberRepository members, UserRepository users) {
		this.teams = teams;
		this.members = members;
		this.users = users;
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

	/** 모든 팀 API는 이 검사를 먼저 통과해야 한다. */
	public TeamMember requireMember(Long teamId, Long userId) {
		return members.findMembership(teamId, userId)
				.orElseThrow(() -> ApiException.forbidden("NOT_TEAM_MEMBER", "이 팀의 구성원이 아닙니다."));
	}

}
