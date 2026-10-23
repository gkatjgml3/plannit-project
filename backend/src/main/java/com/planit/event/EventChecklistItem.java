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

/** 일정의 준비물 한 줄. */
@Entity
@Table(name = "event_checklist_items")
public class EventChecklistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Column(nullable = false, length = 200)
	private String content;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected EventChecklistItem() {
	}

	public EventChecklistItem(Long eventId, String content, int sortOrder) {
		this.eventId = eventId;
		this.content = content;
		this.sortOrder = sortOrder;
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

	public Long getId() {
		return id;
	}

	public Long getEventId() {
		return eventId;
	}

	public String getContent() {
		return content;
	}

	public int getSortOrder() {
		return sortOrder;
	}

}
