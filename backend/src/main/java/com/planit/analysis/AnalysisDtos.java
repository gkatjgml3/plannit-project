package com.planit.analysis;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AnalysisDtos {

	private AnalysisDtos() {
	}

	/**
	 * 후보 전체를 고친 값으로 바꾼다. 빈 문자열은 값 없음으로 본다.
	 * 값이 바뀐 필드는 모호한 필드 목록에서 빠지고, 그대로 두되 확인했다는 필드는 resolvedFields에 넣는다.
	 */
	public record CandidateUpdateRequest(
			@Size(max = 200) String title,
			@Size(max = 32) String startDate,
			@Size(max = 16) String startTime,
			@Size(max = 32) String endDate,
			@Size(max = 16) String endTime,
			@NotBlank @Size(max = 64) String timezone,
			@Size(max = 255) String location,
			@Size(max = 20) List<@NotBlank @Size(max = 80) String> materials,
			@Size(max = 2000) String description,
			boolean recurring,
			@Size(max = 255) String recurrenceNote,
			List<String> resolvedFields) {
	}

	public record ConfirmRequest(@NotEmpty @Size(max = 50) List<@NotNull Long> candidateIds) {
	}

	public record CandidateResponse(
			Long id,
			int position,
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
			String recurrenceNote,
			List<String> uncertainFields,
			List<String> warnings,
			Long eventId) {

		static CandidateResponse of(AnalysisCandidate candidate) {
			AnalysisCandidate.CandidateFields f = candidate.fields();
			return new CandidateResponse(candidate.getId(), candidate.getPosition(), f.title(), f.startDate(),
					f.startTime(), f.endDate(), f.endTime(), f.timezone(), f.location(), f.materials(),
					f.description(), f.recurring(), f.recurrenceNote(), candidate.getUncertainFields(),
					candidate.getWarnings(), candidate.getEventId());
		}

	}

	/** 이미지 저장 키는 내부 값이므로 응답에 넣지 않는다. */
	public record AnalysisResponse(
			Long id,
			String status,
			Long teamId,
			String timezone,
			List<String> warnings,
			String failureCode,
			String failureMessage,
			int attemptCount,
			Instant createdAt,
			Instant updatedAt,
			Instant confirmedAt,
			List<CandidateResponse> candidates) {

		static AnalysisResponse of(ImageAnalysis analysis, List<AnalysisCandidate> candidates) {
			return new AnalysisResponse(analysis.getId(), analysis.getStatus().value(), analysis.getTeamId(),
					analysis.getTimezone(), analysis.getWarnings(), analysis.getFailureCode(),
					analysis.getFailureMessage(), analysis.getAttemptCount(), analysis.getCreatedAt(),
					analysis.getUpdatedAt(), analysis.getConfirmedAt(),
					candidates.stream().map(CandidateResponse::of).toList());
		}

	}

	public record ConfirmResponse(AnalysisResponse analysis, List<Long> createdEventIds) {
	}

	/** 확정할 수 없는 후보 하나의 이유. */
	public record CandidateError(Long candidateId, String code, String message) {
	}

	public record CandidateErrorResponse(String code, String message, List<CandidateError> errors) {
	}

}
