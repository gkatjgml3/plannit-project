package com.planit.notification;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** 알림 outbox 한 행. 제목·메모 같은 개인 내용은 저장하지 않고 일정 id만 가진다. */
@Entity
@Table(name = "notification_outbox")
public class Notification {

	public enum Kind {
		EVENT_REMINDER, TEAM_EVENT_CREATED, TEAM_EVENT_UPDATED, TEAM_EVENT_DELETED
	}

	public enum Status {
		PENDING, SENT, FAILED, CANCELED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Kind kind;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Column(name = "scheduled_at", nullable = false)
	private Instant scheduledAt;

	@Column(name = "next_attempt_at", nullable = false)
	private Instant nextAttemptAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "last_error", length = 100)
	private String lastError;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "read_at")
	private Instant readAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Notification() {
	}

	Notification(Long userId, Kind kind, Long eventId, Instant scheduledAt) {
		this.userId = userId;
		this.kind = kind;
		this.eventId = eventId;
		this.scheduledAt = scheduledAt;
		this.nextAttemptAt = scheduledAt;
		this.status = Status.PENDING;
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

	void reschedule(Instant scheduledAt) {
		this.scheduledAt = scheduledAt;
		this.nextAttemptAt = scheduledAt;
		this.attemptCount = 0;
		this.lastError = null;
	}

	void markSent(Instant now) {
		status = Status.SENT;
		sentAt = now;
		attemptCount++;
		lastError = null;
	}

	/** 입력값: 오류 이름, 다음 시도 시각(null이면 더 시도하지 않음) / 기능: 실패를 기록한다. */
	void markAttemptFailed(String error, Instant nextAttempt) {
		attemptCount++;
		lastError = error;
		if (nextAttempt == null) {
			status = Status.FAILED;
		}
		else {
			nextAttemptAt = nextAttempt;
		}
	}

	void cancel() {
		status = Status.CANCELED;
	}

	void markRead(Instant now) {
		if (readAt == null) {
			readAt = now;
		}
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Kind getKind() {
		return kind;
	}

	public Long getEventId() {
		return eventId;
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public Instant getNextAttemptAt() {
		return nextAttemptAt;
	}

	public Status getStatus() {
		return status;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public String getLastError() {
		return lastError;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
