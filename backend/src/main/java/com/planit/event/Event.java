package com.planit.event;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** teamId가 null이면 개인 일정, 값이 있으면 팀 일정. */
@Entity
@Table(name = "events")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "team_id")
	private Long teamId;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	@Column(nullable = false, length = 64)
	private String timezone;

	@Column(length = 2000)
	private String description;

	@Column(length = 255)
	private String location;

	@Version
	@Column(nullable = false)
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "request_id", length = 100)
	private String requestId;

	protected Event() {
	}

	public Event(Long teamId, Long createdBy, String requestId) {
		this.teamId = teamId;
		this.createdBy = createdBy;
		this.requestId = requestId;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public void update(String title, Instant startsAt, Instant endsAt, String timezone, String description,
			String location) {
		this.title = title;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
		this.timezone = timezone;
		this.description = description;
		this.location = location;
	}

	public void markDeleted() {
		deletedAt = Instant.now();
	}

	public boolean isTeamEvent() {
		return teamId != null;
	}

	public Long getId() {
		return id;
	}

	public Long getTeamId() {
		return teamId;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public String getTitle() {
		return title;
	}

	public Instant getStartsAt() {
		return startsAt;
	}

	public Instant getEndsAt() {
		return endsAt;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

}
