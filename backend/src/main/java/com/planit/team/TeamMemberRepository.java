package com.planit.team;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMember.Id> {

	default Optional<TeamMember> findMembership(Long teamId, Long userId) {
		return findById(new TeamMember.Id(teamId, userId));
	}

	default boolean isMember(Long teamId, Long userId) {
		return existsById(new TeamMember.Id(teamId, userId));
	}

	List<TeamMember> findByIdUserId(Long userId);

	List<TeamMember> findByIdTeamIdOrderByJoinedAt(Long teamId);

}
