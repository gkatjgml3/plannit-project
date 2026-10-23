package com.planit.analysis.analyzer;

import java.util.List;

/**
 * 업체와 무관한 분석 결과. 날짜·시간은 분석기가 읽은 그대로의 문자열이며 검증 전 값이다.
 * 날짜는 "yyyy-MM-dd", 시간은 "HH:mm" 형식을 기대하지만, 서버는 이를 믿지 않고 확정할 때 다시 검증한다.
 */
public record AnalyzerResult(List<CandidateDraft> candidates, List<String> warnings) {

	/**
	 * recurrence는 이미지에 반복 규칙이 분명히 적혀 있을 때만 채운다. 반복한다는 것만 알고 규칙을 모르면
	 * recurring=true, recurrence=null로 두고 recurrenceNote에 읽은 문장을 넣는다.
	 */
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
			List<String> warnings,
			RecurrenceDraft recurrence) {

		/** 반복 규칙 없이 만드는 편의 생성자. */
		public CandidateDraft(String title, String startDate, String startTime, String endDate, String endTime,
				String timezone, String location, List<String> materials, String description, boolean recurring,
				String recurrenceNote, List<String> uncertainFields, List<String> warnings) {
			this(title, startDate, startTime, endDate, endTime, timezone, location, materials, description, recurring,
					recurrenceNote, uncertainFields, warnings, null);
		}

	}

	/** 분석기가 읽은 반복 규칙(검증 전). frequency: DAILY·WEEKLY·MONTHLY, byDays: MO..SU. */
	public record RecurrenceDraft(String frequency, Integer interval, String until, Integer count,
			List<String> byDays) {
	}

}
