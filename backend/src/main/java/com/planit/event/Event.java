package com.planit.event;

import java.time.Instant;
import java.time.LocalDate;

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

	/** 처음 요청한 내용의 SHA-256 값. 같은 Idempotency-Key를 다른 내용에 재사용했는지 판단한다. */
	@Column(name = "request_fingerprint", length = 64)
	private String requestFingerprint;

	/** 반복 규칙(V4). 값이 없으면 반복하지 않는 일정이다. 회차는 조회할 때 펼치며 행으로 저장하지 않는다. */
	@Column(name = "recurrence_frequency", length = 10)
	private String recurrenceFrequency;

	@Column(name = "recurrence_interval")
	private Integer recurrenceInterval;

	@Column(name = "recurrence_until")
	private LocalDate recurrenceUntil;

	@Column(name = "recurrence_count")
	private Integer recurrenceCount;

	@Column(name = "recurrence_by_days", length = 27)
	private String recurrenceByDays;

	protected Event() {
	}

	public Event(Long teamId, Long createdBy, String requestId) {
		this(teamId, createdBy, requestId, null);
	}

	public Event(Long teamId, Long createdBy, String requestId, String requestFingerprint) {
		this.teamId = teamId;
		this.createdBy = createdBy;
		this.requestId = requestId;
		this.requestFingerprint = requestFingerprint;
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

	/** 입력값: 반복 규칙(없으면 null) / 출력값: 없음 / 기능: 반복 규칙을 바꾸거나 지운다. */
	public void setRecurrence(RecurrenceRule rule) {
		this.recurrenceFrequency = rule == null ? null : rule.frequency().name();
		this.recurrenceInterval = rule == null ? null : rule.interval();
		this.recurrenceUntil = rule == null ? null : rule.until();
		this.recurrenceCount = rule == null ? null : rule.count();
		this.recurrenceByDays = rule == null ? null : rule.byDaysColumn();
	}

	/** 입력값: 없음 / 출력값: 반복 규칙, 반복하지 않으면 null / 기능: 저장된 열에서 규칙을 만든다. */
	public RecurrenceRule getRecurrence() {
		return RecurrenceRule.fromColumns(recurrenceFrequency, recurrenceInterval, recurrenceUntil, recurrenceCount,
				recurrenceByDays);
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 준비물만 바뀌어도 version이 올라가도록 수정 시각을 바꾼다. */
	void touch() {
		updatedAt = Instant.now();
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

	public String getRequestFingerprint() {
		return requestFingerprint;
	}

}
