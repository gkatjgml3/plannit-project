package com.planit.analysis;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

import com.planit.analysis.CandidateRules.SanitizedCandidate;
import com.planit.analysis.analyzer.AnalysisInput;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.ScheduleAnalysisException;
import com.planit.analysis.analyzer.ScheduleAnalyzer;
import com.planit.analysis.storage.ImageStorage;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 이미지 분석 실행기. 업로드 요청과 따로 스레드 풀에서 분석을 처리한다.
 * - 분석을 시작할 때 pending → processing으로 바꾸는데, @Version 낙관적 잠금 덕분에 여러 스레드·서버 중 한 곳만 가져간다.
 * - 결과는 자기가 가져간 처리(시작 시각이 같은 processing)일 때만 저장한다. 그 사이 복구 작업이 다시 대기로 돌렸으면 버린다.
 * - 분석기는 DB 트랜잭션 밖에서 호출하므로 오래 걸려도 DB 연결을 붙잡지 않는다.
 * 로그에는 분석 id와 오류 코드만 남긴다.
 */
@Component
public class AnalysisWorker {

	private static final Logger log = LoggerFactory.getLogger(AnalysisWorker.class);
	private static final int BATCH_SIZE = 20;

	private final ImageAnalysisRepository analyses;
	private final AnalysisCandidateRepository candidates;
	private final ScheduleAnalyzer analyzer;
	private final ImageStorage storage;
	private final TransactionTemplate tx;
	private final ThreadPoolTaskExecutor executor;
	private final Duration processingTimeout;

	public AnalysisWorker(ImageAnalysisRepository analyses, AnalysisCandidateRepository candidates,
			ScheduleAnalyzer analyzer, ImageStorage storage, PlatformTransactionManager transactionManager,
			@Value("${planit.analysis.worker-threads:2}") int workerThreads,
			@Value("${planit.analysis.processing-timeout:5m}") Duration processingTimeout) {
		this.analyses = analyses;
		this.candidates = candidates;
		this.analyzer = analyzer;
		this.storage = storage;
		this.tx = new TransactionTemplate(transactionManager);
		this.processingTimeout = processingTimeout;
		this.executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(workerThreads);
		executor.setMaxPoolSize(workerThreads);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("analysis-");
		executor.initialize();
	}

	@PreDestroy
	void shutdown() {
		executor.shutdown();
	}

	/**
	 * 입력값: 분석 id / 출력값: 없음
	 * 기능: 분석을 실행기에 넘긴다. 대기열이 가득 차면 넘기지 않고, 주기적으로 도는 pollPending이 나중에 가져간다.
	 */
	public void submit(Long analysisId) {
		try {
			executor.execute(() -> process(analysisId));
		}
		catch (RejectedExecutionException e) {
			log.warn("분석 대기열이 가득 차 나중에 처리합니다: analysisId={}", analysisId);
		}
	}

	/** 입력값: 없음 / 출력값: 넘긴 분석 수 / 기능: 아직 아무도 가져가지 않은 pending 분석을 실행기에 넘긴다. */
	public int pollPending() {
		List<Long> ids = analyses.findIdsByStatus(AnalysisStatus.PENDING, Limit.of(BATCH_SIZE));
		ids.forEach(this::submit);
		return ids.size();
	}

	/**
	 * 입력값: 기준 시각
	 * 출력값: 복구한 분석 수
	 * 기능: processing-timeout(기본 5분)보다 오래 processing에 머문 분석을 찾아, 시도 횟수가 남았으면 다시 대기로 돌리고
	 * 아니면 실패로 바꾼다. 서버가 처리 도중 꺼져도 분석이 영원히 멈추지 않게 한다.
	 */
	public int recoverStuck(Instant now) {
		List<Long> ids = analyses.findStuckProcessing(now.minus(processingTimeout), Limit.of(BATCH_SIZE));
		List<Long> requeued = new ArrayList<>();
		for (Long id : ids) {
			Boolean back = tx.execute(status -> {
				ImageAnalysis analysis = analyses.findById(id).orElseThrow();
				if (analysis.getStatus() != AnalysisStatus.PROCESSING) {
					return false;
				}
				if (analysis.getAttemptCount() >= AnalysisService.MAX_ATTEMPTS) {
					analysis.markFailed("ANALYSIS_TIMEOUT", "분석이 너무 오래 걸려 중단되었습니다. 이미지를 새로 올려 주세요.");
					return false;
				}
				analysis.markPending();
				return true;
			});
			log.warn("멈춘 분석 복구: analysisId={}, requeued={}", id, back);
			if (Boolean.TRUE.equals(back)) {
				requeued.add(id);
			}
		}
		requeued.forEach(this::submit);
		return ids.size();
	}

	/**
	 * 입력값: 분석 id / 출력값: 없음
	 * 기능: 분석을 가져가 분석기를 호출하고 결과(needs_review) 또는 실패(failed)를 저장한다.
	 */
	void process(Long analysisId) {
		ImageAnalysis claimed = claim(analysisId);
		if (claimed == null) {
			return;
		}
		Instant claimedAt = claimed.getProcessingStartedAt();
		byte[] image;
		try {
			image = storage.load(claimed.getImageKey());
		}
		catch (ImageStorage.ImageNotFoundException e) {
			finishFailed(analysisId, claimedAt, "IMAGE_MISSING", "저장된 이미지가 없어 분석할 수 없습니다. 이미지를 새로 올려 주세요.");
			return;
		}
		catch (RuntimeException e) {
			log.warn("이미지 읽기 실패: analysisId={}, error={}", analysisId, e.getClass().getSimpleName());
			finishFailed(analysisId, claimedAt, "IMAGE_READ_ERROR", "이미지를 읽지 못했습니다. 다시 시도하세요.");
			return;
		}

		AnalyzerResult result;
		try {
			result = analyzer.analyze(new AnalysisInput(image, claimed.getImageContentType(), claimed.getTimezone()));
		}
		catch (ScheduleAnalysisException e) {
			log.warn("이미지 분석 실패: analysisId={}, code={}", analysisId, e.getCode());
			finishFailed(analysisId, claimedAt, e.getCode(), e.getMessage());
			return;
		}
		catch (RuntimeException e) {
			log.warn("이미지 분석 중 예상하지 못한 오류: analysisId={}, error={}", analysisId, e.getClass().getSimpleName());
			finishFailed(analysisId, claimedAt, "AI_UNEXPECTED_ERROR", "AI 분석 중 오류가 발생했습니다. 다시 시도하세요.");
			return;
		}
		if (result == null || result.candidates() == null || result.candidates().stream().anyMatch(Objects::isNull)) {
			log.warn("이미지 분석 응답 형식 오류: analysisId={}", analysisId);
			finishFailed(analysisId, claimedAt, "AI_INVALID_RESPONSE", "AI 응답을 해석하지 못했습니다. 다시 시도하세요.");
			return;
		}
		finishWithResult(analysisId, claimedAt, result);
	}

	/** pending인 분석만 processing으로 바꿔 가져온다. 다른 곳이 먼저 가져갔으면 null. */
	private ImageAnalysis claim(Long analysisId) {
		try {
			return tx.execute(status -> {
				ImageAnalysis analysis = analyses.findById(analysisId).orElse(null);
				if (analysis == null || analysis.getStatus() != AnalysisStatus.PENDING) {
					return null;
				}
				// DB 시각 정밀도(마이크로초)에 맞춰 두어야 나중에 같은 처리인지 비교할 수 있다.
				analysis.markProcessing(Instant.now().truncatedTo(ChronoUnit.MICROS));
				return analyses.saveAndFlush(analysis);
			});
		}
		catch (ObjectOptimisticLockingFailureException e) {
			return null;
		}
	}

	private void finishWithResult(Long analysisId, Instant claimedAt, AnalyzerResult result) {
		tx.executeWithoutResult(status -> {
			ImageAnalysis analysis = analyses.findById(analysisId).orElseThrow();
			if (!stillMine(analysis, claimedAt)) {
				return;
			}
			List<String> warnings = new ArrayList<>(result.warnings() == null ? List.of() : result.warnings());
			List<AnalyzerResult.CandidateDraft> drafts = result.candidates();
			if (drafts.isEmpty()) {
				warnings.add("이미지에서 일정을 찾지 못했습니다.");
			}
			if (drafts.size() > CandidateRules.MAX_CANDIDATES) {
				warnings.add("후보가 너무 많아 앞의 " + CandidateRules.MAX_CANDIDATES + "개만 저장했습니다.");
				drafts = drafts.subList(0, CandidateRules.MAX_CANDIDATES);
			}
			candidates.deleteByAnalysisId(analysisId);
			int position = 0;
			for (AnalyzerResult.CandidateDraft draft : drafts) {
				SanitizedCandidate sanitized = CandidateRules.sanitize(draft, analysis.getTimezone());
				candidates.save(new AnalysisCandidate(analysisId, position++, sanitized.fields(),
						sanitized.uncertainFields(), sanitized.warnings()));
			}
			analysis.markNeedsReview(CandidateRules.limitWarnings(warnings));
		});
	}

	private void finishFailed(Long analysisId, Instant claimedAt, String code, String message) {
		String safeCode = code == null ? "AI_ERROR" : code.substring(0, Math.min(code.length(), 50));
		String safeMessage = message == null ? "AI 분석에 실패했습니다." : message.substring(0, Math.min(message.length(), 500));
		tx.executeWithoutResult(status -> {
			ImageAnalysis analysis = analyses.findById(analysisId).orElseThrow();
			if (stillMine(analysis, claimedAt)) {
				analysis.markFailed(safeCode, safeMessage);
			}
		});
	}

	private boolean stillMine(ImageAnalysis analysis, Instant claimedAt) {
		boolean mine = analysis.getStatus() == AnalysisStatus.PROCESSING
				&& claimedAt.equals(analysis.getProcessingStartedAt());
		if (!mine) {
			log.warn("이미 다른 처리로 넘어간 분석의 결과를 버립니다: analysisId={}", analysis.getId());
		}
		return mine;
	}

}
