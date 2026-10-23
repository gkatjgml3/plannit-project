package com.planit.analysis;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.planit.analysis.storage.ImageStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 원본 이미지 보관 정책. 분석이 확정되면 바로, 확정되지 않은 분석은 업로드 후 retention(기본 7일)이 지나면
 * 저장소에서 이미지를 지운다. 분석 기록과 후보는 그대로 두고 imageDeletedAt만 기록한다.
 * 저장소 삭제가 실패하면 기록하지 않으므로 다음 정리 때 다시 시도한다. 로그에는 분석 id만 남긴다.
 */
@Service
public class ImageRetentionService {

	private static final Logger log = LoggerFactory.getLogger(ImageRetentionService.class);
	private static final int BATCH_SIZE = 100;

	private final ImageAnalysisRepository analyses;
	private final ImageStorage storage;
	private final TransactionTemplate newTx;
	private final Duration retention;

	public ImageRetentionService(ImageAnalysisRepository analyses, ImageStorage storage,
			PlatformTransactionManager transactionManager,
			@Value("${planit.images.retention:7d}") Duration retention) {
		this.analyses = analyses;
		this.storage = storage;
		// 확정 트랜잭션이 커밋된 뒤에도 호출되므로 항상 새 트랜잭션에서 기록한다.
		this.newTx = new TransactionTemplate(transactionManager);
		this.newTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		this.retention = retention;
	}

	/**
	 * 입력값: 분석 id
	 * 출력값: 이번에 지웠으면 true
	 * 기능: 저장소에서 이미지를 지우고 지운 시각을 기록한다. 실패하면 false를 돌려주고 다음 정리 때 다시 시도된다.
	 */
	public boolean deleteImage(Long analysisId) {
		ImageAnalysis analysis = analyses.findById(analysisId).orElse(null);
		if (analysis == null || analysis.getImageDeletedAt() != null) {
			return false;
		}
		try {
			storage.delete(analysis.getImageKey());
		}
		catch (RuntimeException e) {
			log.warn("원본 이미지 삭제 실패, 다음 정리 때 다시 시도: analysisId={}, error={}", analysisId,
					e.getClass().getSimpleName());
			return false;
		}
		newTx.executeWithoutResult(status -> analyses.findById(analysisId)
				.ifPresent(a -> a.markImageDeleted(Instant.now())));
		return true;
	}

	/**
	 * 입력값: 기준 시각
	 * 출력값: 이번에 지운 이미지 수
	 * 기능: 확정된 분석과 보관 기간이 지난 미확정 분석(분석 중인 것은 제외)의 이미지를 지운다.
	 */
	public int cleanup(Instant now) {
		int deleted = 0;
		List<Long> ids = analyses.findImagesToDelete(now.minus(retention), Limit.of(BATCH_SIZE));
		for (Long id : ids) {
			if (deleteImage(id)) {
				deleted++;
			}
		}
		return deleted;
	}

}
