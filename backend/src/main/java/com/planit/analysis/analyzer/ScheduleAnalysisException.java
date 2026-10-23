package com.planit.analysis.analyzer;

/**
 * 분석기가 재시도할 수 있게 실패를 알릴 때 쓰는 예외.
 * code는 개발용(예: AI_PROVIDER_ERROR, AI_INVALID_RESPONSE, AI_TIMEOUT), userMessage는 사용자에게 보여 줄 문장이다.
 * 이미지 내용이나 AI 원문 응답, API 키를 메시지에 넣지 않는다.
 */
public class ScheduleAnalysisException extends RuntimeException {

	private final String code;

	public ScheduleAnalysisException(String code, String userMessage) {
		super(userMessage);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
