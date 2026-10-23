package com.planit.team;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class TeamDtos {

	private TeamDtos() {
	}

	public record TeamRequest(@NotBlank @Size(max = 100) String name) {
	}

	public record TeamResponse(Long id, String name, TeamMember.Role myRole, Instant createdAt) {
	}

	public record AddMemberRequest(@NotBlank @Email String email) {
	}

	public record MemberResponse(Long userId, String email, String name, TeamMember.Role role, Instant joinedAt) {
	}

}
