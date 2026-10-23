package com.planit.event;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 팀 일정을 복사하지 않고, 사용자마다 다른 값(숨김·알림·메모)만 따로 저장한다. */
@Entity
@Table(name = "event_user_settings")
public class EventUserSetting {

	@Embeddable
	public record Id(@Column(name = "event_id") Long eventId, @Column(name = "user_id") Long userId)
			implements Serializable {
	}

	@EmbeddedId
	private Id id;

	@Column(nullable = false)
	private boolean hidden;

	@Column(name = "reminder_at")
	private Instant reminderAt;

	@Column(name = "personal_memo", length = 2000)
	private String personalMemo;

	protected EventUserSetting() {
	}

	public EventUserSetting(Long eventId, Long userId) {
		this.id = new Id(eventId, userId);
	}

	/** 입력값: 새 알림 시각 / 출력값: 없음 / 기능: 일정 시간이 바뀌어 알림 시각만 옮길 때 쓴다. */
	void moveReminder(Instant reminderAt) {
		this.reminderAt = reminderAt;
	}

	public Long getUserId() {
		return id.userId();
	}

	public void update(boolean hidden, Instant reminderAt, String personalMemo) {
		this.hidden = hidden;
		this.reminderAt = reminderAt;
		this.personalMemo = personalMemo;
	}

	public Long getEventId() {
		return id.eventId();
	}

	public boolean isHidden() {
		return hidden;
	}

	public Instant getReminderAt() {
		return reminderAt;
	}

	public String getPersonalMemo() {
		return personalMemo;
	}

}
