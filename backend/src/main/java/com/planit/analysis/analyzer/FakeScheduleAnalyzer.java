package com.planit.analysis.analyzer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 개발·테스트용 가짜 분석기. 실제 AI를 호출하지 않는다.
 * 테스트는 {@link #enqueue}·{@link #enqueueFailure}로 다음 분석 결과를 정해 둘 수 있고,
 * 정해 둔 결과가 없으면 "개발용 예시"라고 표시한 고정 결과를 돌려준다.
 * planit.ai.analyzer=fake(기본값)일 때만 등록된다.
 */
@Component
@ConditionalOnProperty(name = "planit.ai.analyzer", havingValue = "fake", matchIfMissing = true)
public class FakeScheduleAnalyzer implements ScheduleAnalyzer {

	static final String SAMPLE_WARNING = "개발용 가짜 분석기가 만든 예시 결과입니다. 실제 이미지 내용이 아닙니다.";

	private final Deque<Function<AnalysisInput, AnalyzerResult>> scripted = new ConcurrentLinkedDeque<>();

	@Override
	public AnalyzerResult analyze(AnalysisInput input) {
		Function<AnalysisInput, AnalyzerResult> next = scripted.pollFirst();
		return next != null ? next.apply(input) : sample(input);
	}

	/** 입력값: 다음 분석에서 돌려줄 결과 / 출력값: 없음 / 기능: 결과를 순서대로 쌓아 둔다. */
	public void enqueue(AnalyzerResult result) {
		scripted.addLast(input -> result);
	}

	/** 입력값: 개발용 오류 코드 / 출력값: 없음 / 기능: 다음 분석을 재시도 가능한 실패로 만든다. */
	public void enqueueFailure(String code) {
		scripted.addLast(input -> {
			throw new ScheduleAnalysisException(code, "AI 분석에 실패했습니다. 잠시 후 다시 시도하세요.");
		});
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 쌓아 둔 결과를 모두 지운다. */
	public void reset() {
		scripted.clear();
	}

	private static AnalyzerResult sample(AnalysisInput input) {
		LocalDate day = LocalDate.now(ZoneId.of(input.timezone())).plusDays(7);
		return new AnalyzerResult(List.of(
				new CandidateDraft("[예시] 수행평가", day.toString(), "09:00", null, "10:00", input.timezone(),
						null, List.of(), null, false, null, List.of(), List.of()),
				new CandidateDraft("[예시] 날짜를 읽지 못한 일정", null, null, null, null, input.timezone(),
						null, List.of(), null, false, null, List.of("startDate"),
						List.of("날짜를 찾지 못했습니다. 직접 입력하세요."))),
				List.of(SAMPLE_WARNING));
	}

}
