package com.planit.team;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_members")
public class TeamMember {

	public enum Role {
		OWNER, MEMBER
	}

	@Embeddable
	public record Id(@Column(name = "team_id") Long teamId, @Column(name = "user_id") Long userId)
			implements Serializable {
	}

	@EmbeddedId
	private Id id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@Column(name = "joined_at", nullable = false)
	private Instant joinedAt;

	protected TeamMember() {
	}

	public TeamMember(Long teamId, Long userId, Role role) {
		this.id = new Id(teamId, userId);
		this.role = role;
	}

	@PrePersist
	void onCreate() {
		joinedAt = Instant.now();
	}

	public Long getTeamId() {
		return id.teamId();
	}

	public Long getUserId() {
		return id.userId();
	}

	public Role getRole() {
		return role;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

}
