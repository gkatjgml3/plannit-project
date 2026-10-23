package com.planit.analysis;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import com.planit.analysis.AnalysisDtos.AnalysisResponse;
import com.planit.analysis.AnalysisDtos.CandidateResponse;
import com.planit.analysis.AnalysisDtos.CandidateUpdateRequest;
import com.planit.analysis.AnalysisDtos.ConfirmRequest;
import com.planit.analysis.AnalysisDtos.ConfirmResponse;
import com.planit.common.ApiExamples;
import com.planit.common.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "이미지 분석", description = "안내문 이미지 → AI 일정 후보 → 사용자 확인·수정 → 선택한 후보만 일정으로 확정. "
		+ "상태: pending → processing → needs_review → confirmed, 실패하면 failed(재시도 가능). "
		+ "분석은 만든 사람만 조회·수정·확정·재시도할 수 있고, 다른 사람에게는 404 ANALYSIS_NOT_FOUND. "
		+ "AI 제공 업체는 미정이며 지금은 개발용 가짜 분석기가 동작한다.")
public class AnalysisController {

	private final AnalysisService analysisService;

	public AnalysisController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	/**
	 * 입력값: multipart의 image 파일, 선택 teamId, 선택 timezone(기본 Asia/Seoul)
	 * 출력값: 202와 분석(status=pending), Location 헤더
	 * 기능: 이미지를 올려 분석을 요청한다. 분석은 따로 실행되므로 GET /api/analyses/{id}로 상태를 확인한다.
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "분석 요청(이미지 업로드)", description = "JPEG·PNG·WEBP, 10MB 이하. 바로 202와 pending 상태를 돌려주고 "
			+ "분석은 따로 실행된다. GET /api/analyses/{id}로 상태를 확인한다. teamId를 주면 확정한 일정이 그 팀의 일정이 된다(팀원만).\n\n"
			+ "오류: 400 IMAGE_EMPTY, 400 UNSUPPORTED_IMAGE_TYPE, 400 INVALID_TIMEZONE, 400 INVALID_PARAMETER(image 없음), "
			+ "403 NOT_TEAM_MEMBER, 413 IMAGE_TOO_LARGE")
	@ApiResponse(responseCode = "202", content = @Content(examples = @ExampleObject(value = ApiExamples.ANALYSIS_PENDING)))
	@ApiResponse(responseCode = "400", content = @Content(examples = @ExampleObject(value = """
			{"code": "UNSUPPORTED_IMAGE_TYPE", "message": "JPEG, PNG, WEBP 이미지만 올릴 수 있습니다."}""")))
	public ResponseEntity<AnalysisResponse> create(@AuthenticationPrincipal Jwt jwt,
			@Parameter(description = "안내문 이미지 파일") @RequestPart("image") MultipartFile image,
			@Parameter(description = "팀 분석이면 팀 id") @RequestParam(required = false) Long teamId,
			@Parameter(description = "후보의 기본 시간대") @RequestParam(defaultValue = "Asia/Seoul") String timezone) {
		byte[] bytes;
		try {
			bytes = image.getBytes();
		}
		catch (IOException e) {
			throw new UncheckedIOException("업로드한 이미지를 읽지 못했습니다.", e);
		}
		AnalysisResponse created = analysisService.create(CurrentUser.id(jwt), teamId, bytes, timezone);
		return ResponseEntity.accepted().location(URI.create("/api/analyses/" + created.id())).body(created);
	}

	/** 입력값: 분석 id / 출력값: 분석 상태와 후보 / 기능: 분석을 만든 사람만 볼 수 있다. */
	@GetMapping("/{id}")
	@Operation(summary = "분석 상태·후보 조회", description = "uncertainFields는 AI가 확신하지 못한 필드, warnings는 경고. "
			+ "서버는 비어 있는 값을 추측해 채우지 않는다. imageDeleted가 true면 원본 이미지는 보관 정책에 따라 지워졌다.\n\n"
			+ "오류: 404 ANALYSIS_NOT_FOUND")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.ANALYSIS_REVIEW)))
	@ApiResponse(responseCode = "404", content = @Content(examples = @ExampleObject(value = """
			{"code": "ANALYSIS_NOT_FOUND", "message": "분석을 찾을 수 없습니다."}""")))
	public AnalysisResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return analysisService.get(CurrentUser.id(jwt), id);
	}

	/** 입력값: 분석 id, 후보 id, 고친 후보 값 / 출력값: 고친 후보 / 기능: 확정 전에 후보를 고친다. */
	@PutMapping("/{id}/candidates/{candidateId}")
	@Operation(summary = "후보 수정", description = "후보 전체 값을 보낸다(timezone 필수). 값이 바뀐 필드는 uncertainFields에서 빠지고, "
			+ "그대로 두되 확인한 필드는 resolvedFields로 뺀다. needs_review 상태에서만 고칠 수 있다.\n\n"
			+ "오류: 400 VALIDATION_FAILED, 400 INVALID_TIMEZONE, 400 UNKNOWN_FIELD, 404 ANALYSIS_NOT_FOUND, "
			+ "404 CANDIDATE_NOT_FOUND, 409 ANALYSIS_NOT_REVIEWABLE")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.CANDIDATE_UPDATE_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.CANDIDATE)))
	@ApiResponse(responseCode = "409", content = @Content(examples = @ExampleObject(value = """
			{"code": "ANALYSIS_NOT_REVIEWABLE", "message": "검토 중(needs_review)인 분석만 고치거나 확정할 수 있습니다. 현재 상태: confirmed"}""")))
	public CandidateResponse updateCandidate(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@PathVariable Long candidateId, @Valid @RequestBody CandidateUpdateRequest request) {
		return analysisService.updateCandidate(CurrentUser.id(jwt), id, candidateId, request);
	}

	/** 입력값: 분석 id, 확정할 후보 id 목록 / 출력값: 확정 결과와 만든 일정 id / 기능: 선택한 후보만 일정으로 저장한다. */
	@PostMapping("/{id}/confirm")
	@Operation(summary = "선택한 후보 확정", description = "선택한 후보를 모두 검사하고 하나라도 실패하면 아무것도 저장하지 않는다(한 트랜잭션). "
			+ "준비물은 일정의 checklist로, 반복 규칙은 일정의 recurrence로 저장된다. 확정하면 원본 이미지는 바로 지워진다.\n\n"
			+ "후보 오류 코드(errors[].code): CANDIDATE_DATE_MISSING, CANDIDATE_DATE_UNCERTAIN, CANDIDATE_FIELD_UNCERTAIN, "
			+ "CANDIDATE_TITLE_MISSING, INVALID_DATE, INVALID_TIME, INVALID_TIME_RANGE, CANDIDATE_TIME_INCOMPLETE, "
			+ "CANDIDATE_RECURRENCE_MISSING, CANDIDATE_RECURRENCE_INVALID, INVALID_TIMEZONE, CANDIDATE_NOT_FOUND\n\n"
			+ "오류: 400 CANDIDATE_VALIDATION_FAILED, 400 DUPLICATE_CANDIDATE, 403 NOT_TEAM_MEMBER, 404 ANALYSIS_NOT_FOUND, "
			+ "409 ANALYSIS_ALREADY_CONFIRMED, 409 ANALYSIS_NOT_REVIEWABLE")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.CONFIRM_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.CONFIRM_RESPONSE)))
	@ApiResponse(responseCode = "400", content = @Content(examples = @ExampleObject(value = ApiExamples.CANDIDATE_ERRORS)))
	public ConfirmResponse confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
			@Valid @RequestBody ConfirmRequest request) {
		return analysisService.confirm(CurrentUser.id(jwt), id, request.candidateIds());
	}

	/** 입력값: 분석 id / 출력값: 202와 다시 대기 상태가 된 분석 / 기능: 실패한 분석을 다시 시도한다. */
	@PostMapping("/{id}/retry")
	@Operation(summary = "실패한 분석 재시도", description = "failed 상태만 다시 대기(pending)로 돌려 실행기에 넘긴다. 최대 5번 시도.\n\n"
			+ "오류: 404 ANALYSIS_NOT_FOUND, 409 ANALYSIS_NOT_FAILED, 409 RETRY_LIMIT_EXCEEDED, 409 IMAGE_DELETED")
	@ApiResponse(responseCode = "202", content = @Content(examples = @ExampleObject(value = ApiExamples.ANALYSIS_PENDING)))
	public ResponseEntity<AnalysisResponse> retry(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return ResponseEntity.accepted().body(analysisService.retry(CurrentUser.id(jwt), id));
	}

}
