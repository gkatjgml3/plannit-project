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

/**
 * 분석에서 나온 일정 후보 하나. 날짜·시간은 검증 전 문자열 그대로 보관한다.
 * eventId가 있으면 이미 확정되어 events에 저장된 후보다.
 */
@Entity
@Table(name = "analysis_candidates")
public class AnalysisCandidate {

	/** uncertainFields에 쓸 수 있는 필드 이름. */
	public static final List<String> FIELDS = List.of("title", "startDate", "startTime", "endDate", "endTime",
			"timezone", "location", "materials", "description", "recurring");

	/** 이 필드가 모호하면 날짜가 모호한 후보로 보고 확정하지 않는다. */
	static final List<String> DATE_FIELDS = List.of("startDate", "startTime", "endDate", "endTime");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "analysis_id", nullable = false)
	private Long analysisId;

	@Column(nullable = false)
	private int position;

	@Column(length = 200)
	private String title;

	@Column(name = "start_date", length = 32)
	private String startDate;

	@Column(name = "start_time", length = 16)
	private String startTime;

	@Column(name = "end_date", length = 32)
	private String endDate;

	@Column(name = "end_time", length = 16)
	private String endTime;

	@Column(nullable = false, length = 64)
	private String timezone;

	@Column(length = 255)
	private String location;

	@Convert(converter = StringListConverter.class)
	@Column(length = 2000)
	private List<String> materials = List.of();

	@Column(length = 2000)
	private String description;

	@Column(nullable = false)
	private boolean recurring;

	@Column(name = "recurrence_note", length = 255)
	private String recurrenceNote;

	@Convert(converter = StringListConverter.class)
	@Column(name = "uncertain_fields", length = 1000)
	private List<String> uncertainFields = List.of();

	@Convert(converter = StringListConverter.class)
	@Column(length = 2000)
	private List<String> warnings = List.of();

	@Column(name = "event_id")
	private Long eventId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected AnalysisCandidate() {
	}

	AnalysisCandidate(Long analysisId, int position, CandidateFields fields, List<String> uncertainFields,
			List<String> warnings) {
		this.analysisId = analysisId;
		this.position = position;
		apply(fields);
		this.uncertainFields = List.copyOf(uncertainFields);
		this.warnings = List.copyOf(warnings);
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

	/** 사용자가 고친 값을 반영한다. 모호한 필드 목록은 호출하는 쪽에서 계산해 넘긴다. */
	void edit(CandidateFields fields, List<String> remainingUncertainFields) {
		apply(fields);
		this.uncertainFields = List.copyOf(remainingUncertainFields);
	}

	void markConfirmed(Long eventId) {
		this.eventId = eventId;
	}

	CandidateFields fields() {
		return new CandidateFields(title, startDate, startTime, endDate, endTime, timezone, location, materials,
				description, recurring, recurrenceNote);
	}

	private void apply(CandidateFields fields) {
		this.title = fields.title();
		this.startDate = fields.startDate();
		this.startTime = fields.startTime();
		this.endDate = fields.endDate();
		this.endTime = fields.endTime();
		this.timezone = fields.timezone();
		this.location = fields.location();
		this.materials = List.copyOf(fields.materials());
		this.description = fields.description();
		this.recurring = fields.recurring();
		this.recurrenceNote = fields.recurrenceNote();
	}

	public Long getId() {
		return id;
	}

	public Long getAnalysisId() {
		return analysisId;
	}

	public int getPosition() {
		return position;
	}

	public List<String> getUncertainFields() {
		return uncertainFields;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public Long getEventId() {
		return eventId;
	}

	/** 후보의 편집 가능한 값 묶음. */
	record CandidateFields(
			String title,
			String startDate,
			String startTime,
			String endDate,
			String endTime,
			String timezone,
			String location,
			List<String> materials,
			String description,
			boolean recurring,
			String recurrenceNote) {
	}

}
