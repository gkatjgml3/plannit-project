package com.planit.analysis;

import java.util.List;

import com.planit.analysis.AnalysisDtos.CandidateError;

/** 선택한 후보 중 하나라도 확정할 수 없을 때. 아무 후보도 저장되지 않는다. */
class CandidateValidationException extends RuntimeException {

	private final List<CandidateError> errors;

	CandidateValidationException(List<CandidateError> errors) {
		super("확정할 수 없는 후보가 있어 아무 일정도 저장하지 않았습니다.");
		this.errors = List.copyOf(errors);
	}

	List<CandidateError> getErrors() {
		return errors;
	}

}
