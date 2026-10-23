package com.planit.analysis.analyzer;

/**
 * 이미지에서 일정 후보를 읽어 내는 AI 분석기의 경계.
 * 실제 AI 업체(Gemini 등)를 붙일 때는 이 인터페이스의 구현만 추가하고, 업체 응답 형식은 구현 안에서
 * {@link AnalyzerResult}로 바꾼다. 업체 전용 타입이 이 인터페이스 밖으로 나오지 않게 한다.
 */
public interface ScheduleAnalyzer {

	/**
	 * 입력값: 분석할 이미지와 기본 시간대
	 * 출력값: 일정 후보 목록과 경고. 일정이 없으면 빈 목록과 경고를 돌려준다.
	 * 기능: 이미지에 실제로 적힌 값만 후보로 만든다. 읽지 못하거나 애매한 값은 비워 두고 uncertainFields에 넣는다.
	 * 재시도할 수 있는 실패(AI 오류, 잘못된 응답, 시간 초과)는 {@link ScheduleAnalysisException}으로 알린다.
	 */
	AnalyzerResult analyze(AnalysisInput input);

}
