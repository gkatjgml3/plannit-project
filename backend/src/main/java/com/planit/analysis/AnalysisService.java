package com.planit.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.planit.analysis.AnalysisCandidate.CandidateFields;
import com.planit.analysis.AnalysisDtos.AnalysisResponse;
import com.planit.analysis.AnalysisDtos.CandidateError;
import com.planit.analysis.AnalysisDtos.CandidateResponse;
import com.planit.analysis.AnalysisDtos.CandidateUpdateRequest;
import com.planit.analysis.AnalysisDtos.ConfirmResponse;
import com.planit.analysis.CandidateRules.EventFields;
import com.planit.analysis.CandidateRules.SanitizedCandidate;
import com.planit.analysis.analyzer.AnalysisInput;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.ScheduleAnalysisException;
import com.planit.analysis.analyzer.ScheduleAnalyzer;
import com.planit.analysis.storage.ImageStorage;
import com.planit.common.ApiException;
import com.planit.event.Event;
import com.planit.event.EventRepository;
import com.planit.team.TeamService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 이미지 분석 → 후보 검토·수정 → 선택한 후보 확정 흐름.
 * 분석 결과는 분석을 만든 사람만 보고 고치고 확정할 수 있다. 다른 사람에게는 분석이 없는 것처럼 404로 응답한다.
 * 로그에는 분석 id와 오류 코드만 남기고 이미지 내용·저장 키·AI 원문 응답은 남기지 않는다.
 */
@Service
public class AnalysisService {

	private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
	static final int MAX_ATTEMPTS = 5;

	private final ImageAnalysisRepository analyses;
	private final AnalysisCandidateRepository candidates;
	private final EventRepository events;
	private final TeamService teamService;
	private final ScheduleAnalyzer analyzer;
	private final ImageStorage storage;
	private final TransactionTemplate tx;
	private final long maxImageBytes;

	public AnalysisService(ImageAnalysisRepository analyses, AnalysisCandidateRepository candidates,
			EventRepository events, TeamService teamService, ScheduleAnalyzer analyzer, ImageStorage storage,
			PlatformTransactionManager transactionManager,
			@Value("${planit.analysis.max-image-bytes:10485760}") long maxImageBytes) {
		this.analyses = analyses;
		this.candidates = candidates;
		this.events = events;
		this.teamService = teamService;
		this.analyzer = analyzer;
		this.storage = storage;
		this.tx = new TransactionTemplate(transactionManager);
		this.maxImageBytes = maxImageBytes;
	}

	/**
	 * 입력값: 요청한 사용자, 팀 id(개인 분석이면 null), 이미지 바이트, 후보의 기본 시간대
	 * 출력값: 분석 결과(성공하면 needs_review, 분석기가 실패하면 failed)
	 * 기능: 이미지를 검사·저장하고 분석을 실행한다. 팀 분석은 팀원만 만들 수 있다.
	 * 지금은 요청 안에서 바로 분석한다(임시). 오래 걸리는 실제 AI를 붙이면 비동기 실행으로 바꾼다.
	 */
	public AnalysisResponse create(Long userId, Long teamId, byte[] image, String timezone) {
		if (timezone == null || !CandidateRules.isValidZone(timezone)) {
			throw ApiException.badRequest("INVALID_TIMEZONE", "알 수 없는 timezone입니다: " + timezone);
		}
		if (teamId != null) {
			teamService.requireMember(teamId, userId);
		}
		if (image.length == 0) {
			throw ApiException.badRequest("IMAGE_EMPTY", "이미지 파일이 비어 있습니다.");
		}
		if (image.length > maxImageBytes) {
			throw new ApiException(HttpStatus.CONTENT_TOO_LARGE, "IMAGE_TOO_LARGE",
					"이미지는 " + (maxImageBytes / (1024 * 1024)) + "MB 이하만 올릴 수 있습니다.");
		}
		String contentType = ImageTypes.detect(image).orElseThrow(() -> ApiException
				.badRequest("UNSUPPORTED_IMAGE_TYPE", "JPEG, PNG, WEBP 이미지만 올릴 수 있습니다."));

		String imageKey = storage.store(image, contentType);
		ImageAnalysis analysis = analyses
				.save(new ImageAnalysis(userId, teamId, imageKey, contentType, image.length, timezone));
		runAnalysis(analysis.getId(), image, AnalysisStatus.PENDING);
		return load(analysis.getId());
	}

	/** 입력값: 사용자, 분석 id / 출력값: 분석 상태와 후보 목록 / 기능: 소유자만 조회할 수 있다. */
	@Transactional(readOnly = true)
	public AnalysisResponse get(Long userId, Long analysisId) {
		ImageAnalysis analysis = loadOwned(userId, analysisId);
		return AnalysisResponse.of(analysis, candidates.findByAnalysisIdOrderByPosition(analysisId));
	}

	/**
	 * 입력값: 사용자, 분석 id, 후보 id, 고친 값
	 * 출력값: 고친 후보
	 * 기능: needs_review 상태에서만 고칠 수 있다. 값이 바뀐 필드와 resolvedFields는 모호한 필드 목록에서 뺀다.
	 */
	@Transactional
	public CandidateResponse updateCandidate(Long userId, Long analysisId, Long candidateId,
			CandidateUpdateRequest request) {
		ImageAnalysis analysis = loadOwned(userId, analysisId);
		requireReviewable(analysis);
		AnalysisCandidate candidate = candidates.findById(candidateId)
				.filter(c -> c.getAnalysisId().equals(analysisId))
				.orElseThrow(() -> ApiException.notFound("CANDIDATE_NOT_FOUND", "후보를 찾을 수 없습니다."));
		if (!CandidateRules.isValidZone(request.timezone().trim())) {
			throw ApiException.badRequest("INVALID_TIMEZONE", "알 수 없는 timezone입니다: " + request.timezone());
		}
		List<String> resolved = request.resolvedFields() == null ? List.of() : request.resolvedFields();
		for (String field : resolved) {
			if (!AnalysisCandidate.FIELDS.contains(field)) {
				throw ApiException.badRequest("UNKNOWN_FIELD", "알 수 없는 필드입니다: " + field);
			}
		}

		CandidateFields before = candidate.fields();
		CandidateFields after = new CandidateFields(blankToNull(request.title()), blankToNull(request.startDate()),
				blankToNull(request.startTime()), blankToNull(request.endDate()), blankToNull(request.endTime()),
				request.timezone().trim(), blankToNull(request.location()),
				request.materials() == null ? List.of() : request.materials().stream().map(String::trim).toList(),
				blankToNull(request.description()), request.recurring(), blankToNull(request.recurrenceNote()));

		Set<String> settled = new HashSet<>(changedFields(before, after));
		settled.addAll(resolved);
		List<String> remaining = candidate.getUncertainFields().stream().filter(f -> !settled.contains(f)).toList();
		candidate.edit(after, remaining);
		analysis.touch();
		return CandidateResponse.of(candidates.saveAndFlush(candidate));
	}

	/**
	 * 입력값: 사용자, 분석 id, 확정할 후보 id 목록
	 * 출력값: 확정된 분석과 새로 만든 일정 id 목록
	 * 기능: 선택한 후보를 모두 검사한 뒤 하나라도 실패하면 아무것도 저장하지 않는다.
	 * 모두 통과하면 하나의 트랜잭션에서 일정으로 저장한다. 팀 분석은 지금도 팀원일 때만 팀 일정으로 저장한다.
	 */
	@Transactional
	public ConfirmResponse confirm(Long userId, Long analysisId, List<Long> candidateIds) {
		ImageAnalysis analysis = loadOwned(userId, analysisId);
		if (analysis.getStatus() == AnalysisStatus.CONFIRMED) {
			throw ApiException.conflict("ANALYSIS_ALREADY_CONFIRMED", "이미 확정된 분석입니다.");
		}
		requireReviewable(analysis);
		if (new HashSet<>(candidateIds).size() != candidateIds.size()) {
			throw ApiException.badRequest("DUPLICATE_CANDIDATE", "같은 후보를 두 번 선택했습니다.");
		}
		if (analysis.getTeamId() != null) {
			teamService.requireMember(analysis.getTeamId(), userId);
		}

		Map<Long, AnalysisCandidate> byId = candidates.findByAnalysisIdOrderByPosition(analysisId).stream()
				.collect(Collectors.toMap(AnalysisCandidate::getId, Function.identity()));
		List<CandidateError> errors = new ArrayList<>();
		List<AnalysisCandidate> selected = new ArrayList<>();
		List<EventFields> validated = new ArrayList<>();
		for (Long id : candidateIds) {
			AnalysisCandidate candidate = byId.get(id);
			if (candidate == null) {
				errors.add(new CandidateError(id, "CANDIDATE_NOT_FOUND", "이 분석의 후보가 아닙니다."));
				continue;
			}
			EventFields fields = CandidateRules.toEventFields(candidate, errors);
			if (fields != null) {
				selected.add(candidate);
				validated.add(fields);
			}
		}
		if (!errors.isEmpty()) {
			throw new CandidateValidationException(errors);
		}

		List<Long> createdEventIds = new ArrayList<>();
		for (int i = 0; i < selected.size(); i++) {
			EventFields f = validated.get(i);
			Event event = new Event(analysis.getTeamId(), userId, null);
			event.update(f.title(), f.startsAt(), f.endsAt(), f.timezone(), f.description(), f.location());
			Long eventId = events.save(event).getId();
			selected.get(i).markConfirmed(eventId);
			createdEventIds.add(eventId);
		}
		analysis.markConfirmed();
		analyses.flush();
		return new ConfirmResponse(AnalysisResponse.of(analysis, candidates.findByAnalysisIdOrderByPosition(analysisId)),
				createdEventIds);
	}

	/**
	 * 입력값: 사용자, 분석 id
	 * 출력값: 다시 분석한 결과
	 * 기능: failed 상태인 분석만 저장해 둔 이미지로 다시 분석한다. 최대 5번까지 시도할 수 있다.
	 */
	public AnalysisResponse retry(Long userId, Long analysisId) {
		ImageAnalysis analysis = loadOwned(userId, analysisId);
		if (analysis.getStatus() != AnalysisStatus.FAILED) {
			throw ApiException.conflict("ANALYSIS_NOT_FAILED", "실패한 분석만 다시 시도할 수 있습니다.");
		}
		if (analysis.getAttemptCount() >= MAX_ATTEMPTS) {
			throw ApiException.conflict("RETRY_LIMIT_EXCEEDED", "재시도 횟수를 모두 사용했습니다. 이미지를 새로 올려 주세요.");
		}
		byte[] image;
		try {
			image = storage.load(analysis.getImageKey());
		}
		catch (ImageStorage.ImageNotFoundException e) {
			throw ApiException.conflict("IMAGE_MISSING", "저장된 이미지가 없어 다시 분석할 수 없습니다. 이미지를 새로 올려 주세요.");
		}
		runAnalysis(analysisId, image, AnalysisStatus.FAILED);
		return load(analysisId);
	}

	/**
	 * 상태를 processing으로 바꾸고(이때 expected 상태인지 다시 확인), 트랜잭션 밖에서 분석기를 호출한 뒤
	 * 결과를 저장한다. 분석기가 오래 걸려도 DB 트랜잭션을 붙잡고 있지 않는다.
	 */
	private void runAnalysis(Long analysisId, byte[] image, AnalysisStatus expected) {
		ImageAnalysis analysis = tx.execute(status -> {
			ImageAnalysis a = analyses.findById(analysisId).orElseThrow();
			if (a.getStatus() != expected) {
				throw ApiException.conflict("ANALYSIS_STATE_CHANGED", "분석 상태가 바뀌었습니다. 다시 조회하세요.");
			}
			a.markProcessing();
			return analyses.saveAndFlush(a);
		});

		AnalyzerResult result;
		try {
			result = analyzer.analyze(new AnalysisInput(image, analysis.getImageContentType(), analysis.getTimezone()));
		}
		catch (ScheduleAnalysisException e) {
			log.warn("이미지 분석 실패: analysisId={}, code={}", analysisId, e.getCode());
			markFailed(analysisId, e.getCode(), e.getMessage());
			return;
		}
		catch (RuntimeException e) {
			log.warn("이미지 분석 중 예상하지 못한 오류: analysisId={}, error={}", analysisId, e.getClass().getSimpleName());
			markFailed(analysisId, "AI_UNEXPECTED_ERROR", "AI 분석 중 오류가 발생했습니다. 다시 시도하세요.");
			return;
		}
		if (result == null || result.candidates() == null || result.candidates().stream().anyMatch(Objects::isNull)) {
			log.warn("이미지 분석 응답 형식 오류: analysisId={}", analysisId);
			markFailed(analysisId, "AI_INVALID_RESPONSE", "AI 응답을 해석하지 못했습니다. 다시 시도하세요.");
			return;
		}
		saveResult(analysisId, result);
	}

	private void saveResult(Long analysisId, AnalyzerResult result) {
		tx.executeWithoutResult(status -> {
			ImageAnalysis analysis = analyses.findById(analysisId).orElseThrow();
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

	private void markFailed(Long analysisId, String code, String message) {
		String safeCode = code == null ? "AI_ERROR" : code.substring(0, Math.min(code.length(), 50));
		String safeMessage = message == null ? "AI 분석에 실패했습니다." : message.substring(0, Math.min(message.length(), 500));
		tx.executeWithoutResult(status -> {
			ImageAnalysis analysis = analyses.findById(analysisId).orElseThrow();
			analysis.markFailed(safeCode, safeMessage);
		});
	}

	private AnalysisResponse load(Long analysisId) {
		return tx.execute(status -> AnalysisResponse.of(analyses.findById(analysisId).orElseThrow(),
				candidates.findByAnalysisIdOrderByPosition(analysisId)));
	}

	private ImageAnalysis loadOwned(Long userId, Long analysisId) {
		return analyses.findById(analysisId)
				.filter(a -> a.getOwnerId().equals(userId))
				.orElseThrow(() -> ApiException.notFound("ANALYSIS_NOT_FOUND", "분석을 찾을 수 없습니다."));
	}

	private static void requireReviewable(ImageAnalysis analysis) {
		if (analysis.getStatus() != AnalysisStatus.NEEDS_REVIEW) {
			throw ApiException.conflict("ANALYSIS_NOT_REVIEWABLE",
					"검토 중(needs_review)인 분석만 고치거나 확정할 수 있습니다. 현재 상태: " + analysis.getStatus().value());
		}
	}

	private static List<String> changedFields(CandidateFields a, CandidateFields b) {
		List<String> changed = new ArrayList<>();
		addIfChanged(changed, "title", a.title(), b.title());
		addIfChanged(changed, "startDate", a.startDate(), b.startDate());
		addIfChanged(changed, "startTime", a.startTime(), b.startTime());
		addIfChanged(changed, "endDate", a.endDate(), b.endDate());
		addIfChanged(changed, "endTime", a.endTime(), b.endTime());
		addIfChanged(changed, "timezone", a.timezone(), b.timezone());
		addIfChanged(changed, "location", a.location(), b.location());
		addIfChanged(changed, "materials", a.materials(), b.materials());
		addIfChanged(changed, "description", a.description(), b.description());
		addIfChanged(changed, "recurring", a.recurring(), b.recurring());
		return changed;
	}

	private static void addIfChanged(List<String> changed, String field, Object before, Object after) {
		if (!Objects.equals(before, after)) {
			changed.add(field);
		}
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
