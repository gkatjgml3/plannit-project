package com.planit.analysis;

import com.planit.analysis.AnalysisDtos.CandidateErrorResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AnalysisExceptionHandler {

	/** 입력값: 후보 검증 실패 / 출력값: 400과 후보별 이유 / 기능: 어떤 후보를 왜 확정할 수 없는지 알려 준다. */
	@ExceptionHandler(CandidateValidationException.class)
	ResponseEntity<CandidateErrorResponse> handleCandidateValidation(CandidateValidationException e) {
		return ResponseEntity.badRequest()
				.body(new CandidateErrorResponse("CANDIDATE_VALIDATION_FAILED", e.getMessage(), e.getErrors()));
	}

}
