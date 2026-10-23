package com.planit.analysis;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** 이미지 한 장의 분석 작업. teamId가 있으면 확정한 일정이 팀 일정이 된다. */
@Entity
@Table(name = "image_analyses")
public class ImageAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

	@Column(name = "team_id")
	private Long teamId;

	@Convert(converter = AnalysisStatus.DbConverter.class)
	@Column(nullable = false, length = 20)
	private AnalysisStatus status;

	@Column(name = "image_key", nullable = false)
	private String imageKey;

	@Column(name = "image_content_type", nullable = false, length = 100)
	private String imageContentType;

	@Column(name = "image_size_bytes", nullable = false)
	private Long imageSizeBytes;

	@Column(nullable = false, length = 64)
	private String timezone;

	@Convert(converter = StringListConverter.class)
	@Column(length = 4000)
	private List<String> warnings = List.of();

	@Column(name = "failure_code", length = 50)
	private String failureCode;

	@Column(name = "failure_message", length = 500)
	private String failureMessage;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Version
	@Column(nullable = false)
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "confirmed_at")
	private Instant confirmedAt;

	/** 실행기가 분석을 가져간 시각(V7). 오래된 processing을 찾아 복구하는 데 쓴다. */
	@Column(name = "processing_started_at")
	private Instant processingStartedAt;

	/** 원본 이미지를 저장소에서 지운 시각(V6). null이면 아직 저장소에 있다. */
	@Column(name = "image_deleted_at")
	private Instant imageDeletedAt;

	protected ImageAnalysis() {
	}

	public ImageAnalysis(Long ownerId, Long teamId, String imageKey, String imageContentType, long imageSizeBytes,
			String timezone) {
		this.ownerId = ownerId;
		this.teamId = teamId;
		this.imageKey = imageKey;
		this.imageContentType = imageContentType;
		this.imageSizeBytes = imageSizeBytes;
		this.timezone = timezone;
		this.status = AnalysisStatus.PENDING;
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

	void markProcessing(Instant now) {
		status = AnalysisStatus.PROCESSING;
		processingStartedAt = now;
		attemptCount++;
		failureCode = null;
		failureMessage = null;
	}

	/** 재시도나 멈춘 작업 복구로 다시 대기 상태로 돌린다. */
	void markPending() {
		status = AnalysisStatus.PENDING;
		processingStartedAt = null;
		failureCode = null;
		failureMessage = null;
	}

	void markNeedsReview(List<String> warnings) {
		status = AnalysisStatus.NEEDS_REVIEW;
		this.warnings = List.copyOf(warnings);
	}

	void markFailed(String code, String message) {
		status = AnalysisStatus.FAILED;
		failureCode = code;
		failureMessage = message;
	}

	void markConfirmed() {
		status = AnalysisStatus.CONFIRMED;
		confirmedAt = Instant.now();
	}

	void markImageDeleted(Instant now) {
		imageDeletedAt = now;
	}

	/** 후보를 고쳤을 때 버전을 올려, 동시에 진행 중인 확정 요청이 오래된 후보로 저장되지 않게 한다. */
	void touch() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public Long getTeamId() {
		return teamId;
	}

	public AnalysisStatus getStatus() {
		return status;
	}

	public String getImageKey() {
		return imageKey;
	}

	public String getImageContentType() {
		return imageContentType;
	}

	public String getTimezone() {
		return timezone;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public String getFailureCode() {
		return failureCode;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getConfirmedAt() {
		return confirmedAt;
	}

	public Instant getProcessingStartedAt() {
		return processingStartedAt;
	}

	public Instant getImageDeletedAt() {
		return imageDeletedAt;
	}

}
