package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;
import com.planit.analysis.analyzer.AnalysisInput;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;
import com.planit.analysis.analyzer.FakeScheduleAnalyzer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

class AnalysisApiTest extends ApiTestSupport {

	/** PNG 시그니처로 시작하는 테스트용 바이트. 가짜 분석기는 내용을 읽지 않는다. */
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };

	@Autowired
	private FakeScheduleAnalyzer analyzer;

	private TestUser user;

	@BeforeEach
	void setUp() throws Exception {
		analyzer.reset();
		user = signupAndLogin("analysis");
	}

	@AfterEach
	void tearDown() {
		analyzer.reset();
	}

	private static CandidateDraft draft(String title, String date, String startTime, String endTime) {
		return new CandidateDraft(title, date, startTime, null, endTime, null, null, List.of(), null, false, null,
				List.of(), List.of());
	}

	private static AnalyzerResult result(CandidateDraft... drafts) {
		return new AnalyzerResult(List.of(drafts), List.of());
	}

	private ResultActions upload(TestUser by, Long teamId) throws Exception {
		var request = multipart("/api/analyses").file(new MockMultipartFile("image", "notice.png", "image/png", PNG))
				.header("Authorization", by.bearer());
		if (teamId != null) {
			request.param("teamId", teamId.toString());
		}
		return mvc.perform(request);
	}

	private MvcResult analyze(AnalyzerResult scripted) throws Exception {
		analyzer.enqueue(scripted);
		return upload(user, null).andExpect(status().isCreated()).andReturn();
	}

	private static List<Long> candidateIds(MvcResult result) throws Exception {
		List<Number> ids = JsonPath.read(result.getResponse().getContentAsString(), "$.candidates[*].id");
		return ids.stream().map(Number::longValue).toList();
	}

	private ResultActions confirm(TestUser by, long analysisId, List<Long> ids) throws Exception {
		String body = ids.stream().map(String::valueOf).collect(Collectors.joining(",", "{\"candidateIds\":[", "]}"));
		return mvc.perform(post("/api/analyses/{id}/confirm", analysisId).header("Authorization", by.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private long countEvents(TestUser by) {
		return jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, by.id());
	}

	@Test
	void oneImageProducesSeveralCandidates() throws Exception {
		CandidateDraft detailed = new CandidateDraft("과학 수행평가", "2026-12-01", "09:00", null, "10:00",
				"Asia/Seoul", "과학실", List.of("실험복", "보고서"), "3단원 범위", false, null, List.of("location"),
				List.of("장소 글씨가 흐립니다."));
		MvcResult created = analyze(new AnalyzerResult(List.of(detailed, draft("수학 시험", "2026-12-03", "10:00", "11:00"),
				new CandidateDraft("영어 단어 시험", "2026-12-05", null, null, null, null, null, List.of(), null, true,
						"매주 금요일", List.of(), List.of())),
				List.of("표 일부가 잘렸습니다.")));

		mvc.perform(get("/api/analyses/{id}", idOf(created)).header("Authorization", user.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.warnings[0]").value("표 일부가 잘렸습니다."))
				.andExpect(jsonPath("$.candidates", hasSize(3)))
				.andExpect(jsonPath("$.candidates[0].title").value("과학 수행평가"))
				.andExpect(jsonPath("$.candidates[0].materials[1]").value("보고서"))
				.andExpect(jsonPath("$.candidates[0].uncertainFields[0]").value("location"))
				.andExpect(jsonPath("$.candidates[0].warnings[0]").value("장소 글씨가 흐립니다."))
				.andExpect(jsonPath("$.candidates[0].timezone").value("Asia/Seoul"))
				.andExpect(jsonPath("$.candidates[2].recurring").value(true))
				.andExpect(jsonPath("$.candidates[2].recurrenceNote").value("매주 금요일"))
				.andExpect(jsonPath("$.candidates[*].eventId", hasItem(nullValue())))
				.andExpect(jsonPath("$.imageKey").doesNotExist());
		assertThat(countEvents(user)).isZero();
	}

	@Test
	void serverDoesNotFillMissingValues() throws Exception {
		MvcResult created = analyze(result(new CandidateDraft("날짜 없는 공지", null, null, null, null, null, null, null,
				null, false, null, List.of("startDate", "unknownField"), null)));

		mvc.perform(get("/api/analyses/{id}", idOf(created)).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.candidates[0].startDate").doesNotExist())
				.andExpect(jsonPath("$.candidates[0].location").doesNotExist())
				.andExpect(jsonPath("$.candidates[0].uncertainFields", hasSize(1)))
				.andExpect(jsonPath("$.candidates[0].uncertainFields[0]").value("startDate"));
	}

	@Test
	void candidateWithoutDateCannotBeConfirmed() throws Exception {
		MvcResult created = analyze(result(draft("날짜 없음", null, null, null)));

		confirm(user, idOf(created), candidateIds(created))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANDIDATE_VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[0].code").value("CANDIDATE_DATE_MISSING"));
		assertThat(countEvents(user)).isZero();
	}

	@Test
	void ambiguousDateBlocksConfirmUntilUserFixesIt() throws Exception {
		MvcResult created = analyze(result(new CandidateDraft("모호한 날짜", "2026-12-10", null, null, null, null, null,
				List.of(), null, false, null, List.of("startDate"), List.of("날짜가 10일인지 18일인지 불분명합니다."))));
		long analysisId = idOf(created);
		long candidateId = candidateIds(created).get(0);

		confirm(user, analysisId, List.of(candidateId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("CANDIDATE_DATE_UNCERTAIN"));

		mvc.perform(put("/api/analyses/{id}/candidates/{cid}", analysisId, candidateId)
				.header("Authorization", user.bearer()).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"모호한 날짜","startDate":"2026-12-18","timezone":"Asia/Seoul","recurring":false}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.startDate").value("2026-12-18"))
				.andExpect(jsonPath("$.uncertainFields", hasSize(0)))
				.andExpect(jsonPath("$.warnings[0]").value("날짜가 10일인지 18일인지 불분명합니다."));

		confirm(user, analysisId, List.of(candidateId)).andExpect(status().isOk());
	}

	@Test
	void userCanAcceptUncertainValueAsIs() throws Exception {
		MvcResult created = analyze(result(new CandidateDraft("장소 확인", "2026-12-11", "09:00", null, "10:00", null,
				"강당", List.of(), null, false, null, List.of("location"), List.of())));
		long analysisId = idOf(created);
		long candidateId = candidateIds(created).get(0);

		confirm(user, analysisId, List.of(candidateId))
				.andExpect(jsonPath("$.errors[0].code").value("CANDIDATE_FIELD_UNCERTAIN"));
		mvc.perform(put("/api/analyses/{id}/candidates/{cid}", analysisId, candidateId)
				.header("Authorization", user.bearer()).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"장소 확인","startDate":"2026-12-11","startTime":"09:00","endTime":"10:00",
						 "timezone":"Asia/Seoul","location":"강당","recurring":false,"resolvedFields":["location"]}
						"""))
				.andExpect(jsonPath("$.uncertainFields", hasSize(0)));
		confirm(user, analysisId, List.of(candidateId)).andExpect(status().isOk());
	}

	@Test
	void impossibleDateIsRejected() throws Exception {
		MvcResult created = analyze(result(draft("2월 30일 시험", "2026-02-30", "09:00", "10:00")));

		mvc.perform(get("/api/analyses/{id}", idOf(created)).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.candidates[0].startDate").value("2026-02-30"))
				.andExpect(jsonPath("$.candidates[0].warnings[0]", containsString("존재하지 않는 날짜")));
		confirm(user, idOf(created), candidateIds(created))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_DATE"));
		assertThat(countEvents(user)).isZero();
	}

	@Test
	void reversedTimeIsRejected() throws Exception {
		MvcResult created = analyze(result(draft("거꾸로", "2026-12-01", "15:00", "14:00")));

		confirm(user, idOf(created), candidateIds(created))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TIME_RANGE"));
		assertThat(countEvents(user)).isZero();
	}

	@Test
	void onlySelectedCandidatesAreSaved() throws Exception {
		MvcResult created = analyze(result(draft("1번", "2026-12-01", "09:00", "10:00"),
				draft("2번", "2026-12-02", "09:00", "10:00"), draft("3번", "2026-12-03", "09:00", "10:00")));
		long analysisId = idOf(created);
		List<Long> ids = candidateIds(created);

		MvcResult confirmed = confirm(user, analysisId, List.of(ids.get(0), ids.get(2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.analysis.status").value("confirmed"))
				.andExpect(jsonPath("$.createdEventIds", hasSize(2)))
				.andExpect(jsonPath("$.analysis.candidates[0].eventId").isNumber())
				.andExpect(jsonPath("$.analysis.candidates[1].eventId").doesNotExist())
				.andExpect(jsonPath("$.analysis.candidates[2].eventId").isNumber())
				.andReturn();

		assertThat(countEvents(user)).isEqualTo(2);
		assertThat(jdbc.queryForList("select title from events where created_by = ? order by starts_at", String.class,
				user.id())).containsExactly("1번", "3번");
		assertThat(confirmed.getResponse().getContentAsString()).doesNotContain("imageKey");

		// 이미 확정한 분석은 다시 확정하거나 고칠 수 없다.
		confirm(user, analysisId, List.of(ids.get(1)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ANALYSIS_ALREADY_CONFIRMED"));
		assertThat(countEvents(user)).isEqualTo(2);
	}

	@Test
	void oneInvalidCandidateCancelsWholeConfirm() throws Exception {
		MvcResult created = analyze(result(draft("정상", "2026-12-01", "09:00", "10:00"),
				draft("정상 2", "2026-12-02", "09:00", "10:00"), draft("잘못된 날짜", "2026-13-01", "09:00", "10:00")));
		long analysisId = idOf(created);

		confirm(user, analysisId, candidateIds(created))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].candidateId").value(candidateIds(created).get(2)));

		assertThat(countEvents(user)).isZero();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.candidates[0].eventId").doesNotExist());
	}

	@Test
	void candidateFromAnotherAnalysisIsRejected() throws Exception {
		MvcResult first = analyze(result(draft("첫 분석", "2026-12-01", "09:00", "10:00")));
		MvcResult second = analyze(result(draft("둘째 분석", "2026-12-02", "09:00", "10:00")));

		confirm(user, idOf(first), List.of(candidateIds(first).get(0), candidateIds(second).get(0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("CANDIDATE_NOT_FOUND"));
		assertThat(countEvents(user)).isZero();
	}

	@Test
	void otherUsersCannotReadEditConfirmOrRetry() throws Exception {
		MvcResult created = analyze(result(draft("내 분석", "2026-12-01", "09:00", "10:00")));
		long analysisId = idOf(created);
		long candidateId = candidateIds(created).get(0);
		TestUser stranger = signupAndLogin("stranger");

		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", stranger.bearer()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ANALYSIS_NOT_FOUND"));
		mvc.perform(put("/api/analyses/{id}/candidates/{cid}", analysisId, candidateId)
				.header("Authorization", stranger.bearer()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"가로채기\",\"timezone\":\"Asia/Seoul\",\"recurring\":false}"))
				.andExpect(status().isNotFound());
		confirm(stranger, analysisId, List.of(candidateId)).andExpect(status().isNotFound());
		mvc.perform(post("/api/analyses/{id}/retry", analysisId).header("Authorization", stranger.bearer()))
				.andExpect(status().isNotFound());

		assertThat(countEvents(stranger)).isZero();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.candidates[0].title").value("내 분석"));
	}

	@Test
	void failedAnalysisCanBeRetried() throws Exception {
		analyzer.enqueueFailure("AI_TIMEOUT");
		MvcResult created = upload(user, null)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("failed"))
				.andExpect(jsonPath("$.failureCode").value("AI_TIMEOUT"))
				.andExpect(jsonPath("$.attemptCount").value(1))
				.andExpect(jsonPath("$.candidates", hasSize(0)))
				.andReturn();
		long analysisId = idOf(created);

		analyzer.enqueue(result(draft("다시 읽은 일정", "2026-12-01", "09:00", "10:00")));
		mvc.perform(post("/api/analyses/{id}/retry", analysisId).header("Authorization", user.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.failureCode").doesNotExist())
				.andExpect(jsonPath("$.attemptCount").value(2))
				.andExpect(jsonPath("$.candidates[0].title").value("다시 읽은 일정"));

		// 실패 상태가 아니면 재시도할 수 없다.
		mvc.perform(post("/api/analyses/{id}/retry", analysisId).header("Authorization", user.bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ANALYSIS_NOT_FAILED"));
	}

	@Test
	void retryIsLimited() throws Exception {
		for (int i = 0; i < 5; i++) {
			analyzer.enqueueFailure("AI_PROVIDER_ERROR");
		}
		long analysisId = idOf(upload(user, null).andReturn());
		for (int attempt = 2; attempt <= 5; attempt++) {
			mvc.perform(post("/api/analyses/{id}/retry", analysisId).header("Authorization", user.bearer()))
					.andExpect(jsonPath("$.attemptCount").value(attempt));
		}
		mvc.perform(post("/api/analyses/{id}/retry", analysisId).header("Authorization", user.bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("RETRY_LIMIT_EXCEEDED"));
	}

	@Test
	void confirmedEventsAppearInCalendar() throws Exception {
		MvcResult created = analyze(result(
				new CandidateDraft("체험학습", "2026-12-15", "08:30", null, "16:00", null, "국립과학관",
						List.of("도시락", "물"), "학년 전체", false, null, List.of(), List.of()),
				draft("방학식", "2026-12-24", null, null)));

		confirm(user, idOf(created), candidateIds(created)).andExpect(status().isOk());

		mvc.perform(get("/api/events").param("from", "2026-12-01").param("to", "2026-12-31")
				.header("Authorization", user.bearer()))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].title").value("체험학습"))
				.andExpect(jsonPath("$[0].startsAt").value("2026-12-15T08:30:00+09:00"))
				.andExpect(jsonPath("$[0].endsAt").value("2026-12-15T16:00:00+09:00"))
				.andExpect(jsonPath("$[0].location").value("국립과학관"))
				.andExpect(jsonPath("$[0].description").value("학년 전체\n준비물: 도시락, 물"))
				.andExpect(jsonPath("$[0].teamId").doesNotExist())
				// 시간이 없는 후보는 종일 일정으로 저장된다.
				.andExpect(jsonPath("$[1].title").value("방학식"))
				.andExpect(jsonPath("$[1].startsAt").value("2026-12-24T00:00:00+09:00"))
				.andExpect(jsonPath("$[1].endsAt").value("2026-12-25T00:00:00+09:00"));
	}

	@Test
	void personalAnalysisBecomesPersonalEventInvisibleToTeammates() throws Exception {
		TestUser teammate = signupAndLogin("teammate");
		long teamId = createTeamWith(teammate);
		MvcResult created = analyze(result(draft("개인 분석", "2026-12-01", "09:00", "10:00")));

		confirm(user, idOf(created), candidateIds(created)).andExpect(status().isOk());

		mvc.perform(get("/api/events").param("from", "2026-12-01").param("to", "2026-12-31")
				.header("Authorization", teammate.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
		assertThat(jdbc.queryForObject("select count(*) from events where team_id = ?", Long.class, teamId)).isZero();
	}

	@Test
	void teamAnalysisBecomesTeamEventForMembersOnly() throws Exception {
		TestUser teammate = signupAndLogin("teammate");
		TestUser outsider = signupAndLogin("outsider");
		long teamId = createTeamWith(teammate);

		// 팀원이 아니면 팀 분석을 만들 수 없다.
		upload(outsider, teamId)
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("NOT_TEAM_MEMBER"));

		analyzer.enqueue(result(draft("조별 발표", "2026-12-04", "13:00", "14:00")));
		MvcResult created = upload(user, teamId)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.teamId").value(teamId))
				.andReturn();

		// 같은 팀원이라도 분석 결과는 분석을 만든 사람만 본다.
		mvc.perform(get("/api/analyses/{id}", idOf(created)).header("Authorization", teammate.bearer()))
				.andExpect(status().isNotFound());

		confirm(user, idOf(created), candidateIds(created)).andExpect(status().isOk());

		mvc.perform(get("/api/events").param("from", "2026-12-01").param("to", "2026-12-31")
				.header("Authorization", teammate.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].teamId").value(teamId))
				.andExpect(jsonPath("$[0].createdBy").value(user.id()));
		mvc.perform(get("/api/events").param("from", "2026-12-01").param("to", "2026-12-31")
				.header("Authorization", outsider.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
		assertThat(jdbc.queryForObject("select count(*) from events where team_id = ?", Long.class, teamId))
				.isEqualTo(1);

		// 확정된 팀 일정에는 기존 팀 일정 수정 정책이 그대로 적용된다.
		long eventId = ((Number) JsonPath.read(mvc.perform(get("/api/events").param("from", "2026-12-01")
				.param("to", "2026-12-31").header("Authorization", user.bearer())).andReturn()
				.getResponse().getContentAsString(), "$[0].id")).longValue();
		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", teammate.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"바꾸기","startsAt":"2026-12-04T13:00:00+09:00","endsAt":"2026-12-04T14:00:00+09:00",
						 "timezone":"Asia/Seoul","version":0}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void unsupportedOrEmptyFilesAreRejected() throws Exception {
		mvc.perform(multipart("/api/analyses")
				.file(new MockMultipartFile("image", "notice.png", "image/png", "not an image".getBytes()))
				.header("Authorization", user.bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("UNSUPPORTED_IMAGE_TYPE"));
		mvc.perform(multipart("/api/analyses")
				.file(new MockMultipartFile("image", "empty.png", "image/png", new byte[0]))
				.header("Authorization", user.bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("IMAGE_EMPTY"));
		mvc.perform(multipart("/api/analyses").header("Authorization", user.bearer()))
				.andExpect(status().isBadRequest());
		mvc.perform(multipart("/api/analyses").file(new MockMultipartFile("image", "a.png", "image/png", PNG)))
				.andExpect(status().isUnauthorized());
		assertThat(jdbc.queryForObject("select count(*) from image_analyses where owner_id = ?", Long.class, user.id()))
				.isZero();
	}

	@Test
	void noScheduleFoundIsReviewableWithWarning() throws Exception {
		MvcResult created = analyze(new AnalyzerResult(List.of(), List.of()));
		mvc.perform(get("/api/analyses/{id}", idOf(created)).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.candidates", hasSize(0)))
				.andExpect(jsonPath("$.warnings[0]").value("이미지에서 일정을 찾지 못했습니다."));
	}

	@Test
	void analysisInputNeverPrintsImageBytes() {
		AnalysisInput input = new AnalysisInput("학생 이름 홍길동".getBytes(), "image/png", "Asia/Seoul");
		assertThat(input.toString()).doesNotContain("홍길동").contains("size=");
	}

	@Test
	void defaultFakeResultIsMarkedAsSample() throws Exception {
		upload(user, null)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.warnings[0]", containsString("가짜 분석기")))
				.andExpect(jsonPath("$.candidates[0].title", containsString("[예시]")))
				.andExpect(jsonPath("$.candidates[1].startDate").doesNotExist())
				.andExpect(jsonPath("$.candidates[1].uncertainFields", not(hasSize(0))));
	}

	private long createTeamWith(TestUser member) throws Exception {
		long teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"2학년 1반\"}"))
				.andReturn());
		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
				.andExpect(status().isCreated());
		return teamId;
	}

}
