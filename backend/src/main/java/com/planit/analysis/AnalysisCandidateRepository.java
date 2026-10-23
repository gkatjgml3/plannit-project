package com.planit.analysis;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisCandidateRepository extends JpaRepository<AnalysisCandidate, Long> {

	List<AnalysisCandidate> findByAnalysisIdOrderByPosition(Long analysisId);

	void deleteByAnalysisId(Long analysisId);

}
