package com.planit.analysis;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageAnalysisRepository extends JpaRepository<ImageAnalysis, Long> {

	/**
	 * 입력값: 미확정 이미지를 지울 기준 시각, 최대 개수 / 출력값: 이미지를 지워야 하는 분석 id
	 * 기능: 확정된 분석, 또는 기준 시각 전에 올렸고 지금 분석 중이 아닌 분석 가운데 이미지가 아직 남은 것을 찾는다.
	 */
	@Query("""
			select a.id from ImageAnalysis a
			where a.imageDeletedAt is null
			  and (a.status = com.planit.analysis.AnalysisStatus.CONFIRMED
			       or (a.createdAt < :cutoff
			           and a.status in (com.planit.analysis.AnalysisStatus.NEEDS_REVIEW,
			                            com.planit.analysis.AnalysisStatus.FAILED)))
			order by a.id
			""")
	List<Long> findImagesToDelete(@Param("cutoff") Instant cutoff, Limit limit);

	/** 입력값: 상태, 최대 개수 / 출력값: 오래된 순 분석 id / 기능: 실행기가 놓친 대기 분석을 찾는다. */
	@Query("select a.id from ImageAnalysis a where a.status = :status order by a.id")
	List<Long> findIdsByStatus(@Param("status") AnalysisStatus status, Limit limit);

	/** 입력값: 기준 시각 / 출력값: 그보다 먼저 처리를 시작해 아직 processing인 분석 id / 기능: 멈춘 분석을 찾는다. */
	@Query("""
			select a.id from ImageAnalysis a
			where a.status = com.planit.analysis.AnalysisStatus.PROCESSING and a.processingStartedAt < :cutoff
			order by a.id
			""")
	List<Long> findStuckProcessing(@Param("cutoff") Instant cutoff, Limit limit);

}
