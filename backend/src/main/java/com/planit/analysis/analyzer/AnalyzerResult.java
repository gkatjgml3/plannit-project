package com.planit.analysis.analyzer;

import java.util.List;

/**
 * 업체와 무관한 분석 결과. 날짜·시간은 분석기가 읽은 그대로의 문자열이며 검증 전 값이다.
 * 날짜는 "yyyy-MM-dd", 시간은 "HH:mm" 형식을 기대하지만, 서버는 이를 믿지 않고 확정할 때 다시 검증한다.
 */
public record AnalyzerResult(List<CandidateDraft> candidates, List<String> warnings) {

	public record CandidateDraft(
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
			List<String> warnings) {
	}

}
