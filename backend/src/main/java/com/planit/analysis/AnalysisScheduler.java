package com.planit.analysis;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 이미지 분석 관련 주기 작업: 원본 이미지 정리, 놓친 대기 분석 처리, 멈춘 분석 복구. */
@Component
@ConditionalOnProperty(name = "planit.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AnalysisScheduler {

	private final ImageRetentionService retention;
	private final AnalysisWorker worker;

	public AnalysisScheduler(ImageRetentionService retention, AnalysisWorker worker) {
		this.retention = retention;
		this.worker = worker;
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 지워야 할 원본 이미지를 정리한다. 실패한 삭제도 여기서 다시 시도된다. */
	@Scheduled(fixedDelayString = "${planit.images.cleanup-interval:1h}", initialDelayString = "1m")
	public void cleanupImages() {
		retention.cleanup(Instant.now());
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 서버가 다시 켜졌을 때 멈춘 분석을 복구하고 남은 대기 분석을 처리한다. */
	@EventListener(ApplicationReadyEvent.class)
	public void onStartup() {
		recoverAndPoll();
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 오래된 processing을 복구하고 아무도 가져가지 않은 pending을 실행기에 넘긴다. */
	@Scheduled(fixedDelayString = "${planit.analysis.poll-interval:10s}", initialDelayString = "10s")
	public void recoverAndPoll() {
		worker.recoverStuck(Instant.now());
		worker.pollPending();
	}

}
