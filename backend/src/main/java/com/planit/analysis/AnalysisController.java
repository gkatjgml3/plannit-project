package com.planit.analysis;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import com.planit.analysis.AnalysisDtos.AnalysisResponse;
import com.planit.analysis.AnalysisDtos.CandidateResponse;
import com.planit.analysis.AnalysisDtos.CandidateUpdateRequest;
import com.planit.analysis.AnalysisDtos.ConfirmRequest;
import com.planit.analysis.AnalysisDtos.ConfirmResponse;
import com.planit.common.CurrentUser;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

	private final AnalysisService analysisService;

	public AnalysisController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	/**
	 * 입력값: multipart의 image 파일, 선택 teamId, 선택 timezone(기본 Asia/Seoul)
	 * 출력값: 201과 분석 결과(needs_review 또는 failed)
	 * 기능: 이미지를 올려 분석을 시작한다.
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AnalysisResponse> create(@AuthenticationPrincipal Jwt jwt,
			@RequestPart("image") MultipartFile image,
			@RequestParam(required = false) Long teamId,
			@RequestParam(defaultValue = "Asia/Seoul") String timezone) {
		byte[] bytes;
		try {
			bytes = image.getBytes();
		}
		catch (IOException e) {
			throw new UncheckedIOException("업로드한 이미지를 읽지 못했습니다.", e);
		}
		AnalysisResponse created = analysisService.create(CurrentUser.id(jwt), teamId, bytes, timezone);
		return ResponseEntity.created(URI.create("/api/analyses/" + created.id())).body(created);
	}

	/** 입력값: 분석 id / 출력값: 분석 상태와 후보 / 기능: 분석을 만든 사람만 볼 수 있다. */
	@GetMapping("/{id}")
	public AnalysisResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return analysisService.get(CurrentUser.id(jwt), id);
	}

	/** 입력값: 분석 id, 후보 id, 고친 후보 값 / 출력값: 고친 후보 / 기능: 확정 전에 후보를 고친다. */
	@PutMapping("/{id}/candidates/{candidateId}")
	public CandidateResponse updateCandidate(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@PathVariable Long candidateId, @Valid @RequestBody CandidateUpdateRequest request) {
		return analysisService.updateCandidate(CurrentUser.id(jwt), id, candidateId, request);
	}

	/** 입력값: 분석 id, 확정할 후보 id 목록 / 출력값: 확정 결과와 만든 일정 id / 기능: 선택한 후보만 일정으로 저장한다. */
	@PostMapping("/{id}/confirm")
	public ConfirmResponse confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody ConfirmRequest request) {
		return analysisService.confirm(CurrentUser.id(jwt), id, request.candidateIds());
	}

	/** 입력값: 분석 id / 출력값: 다시 분석한 결과 / 기능: 실패한 분석을 다시 시도한다. */
	@PostMapping("/{id}/retry")
	public AnalysisResponse retry(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return analysisService.retry(CurrentUser.id(jwt), id);
	}

}
