package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.planit.analysis.AnalysisWorker;
import com.planit.analysis.analyzer.AnalysisInput;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;
import com.planit.analysis.analyzer.FakeScheduleAnalyzer;
import com.planit.analysis.analyzer.ScheduleAnalysisException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

class AsyncAnalysisTest extends ApiTestSupport {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };

	@Autowired
	private FakeScheduleAnalyzer analyzer;

	@Autowired
	private AnalysisWorker worker;

	private TestUser user;

	@BeforeEach
	void setUp() throws Exception {
		analyzer.reset();
		user = signupAndLogin("async");
	}

	@AfterEach
	void tearDown() {
		analyzer.reset();
	}

	private static AnalyzerResult one(String title) {
		return new AnalyzerResult(List.of(new CandidateDraft(title, "2026-12-01", "09:00", null, "10:00", null, null,
				List.of(), null, false, null, List.of(), List.of())), List.of());
	}

	private ResultActions upload(TestUser by) throws Exception {
		return mvc.perform(multipart("/api/analyses").file(new MockMultipartFile("image", "n.png", "image/png", PNG))
				.header("Authorization", by.bearer()));
	}

	private ResultActions get(TestUser by, long analysisId) throws Exception {
		return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get("/api/analyses/{id}", analysisId).header("Authorization", by.bearer()));
	}

	private void forceStatus(long analysisId, String status, Instant processingStartedAt, int attempts) {
		jdbc.update("update image_analyses set status = ?, processing_started_at = ?, attempt_count = ?, "
				+ "version = version + 1 where id = ?", status,
				processingStartedAt == null ? null : Timestamp.from(processingStartedAt), attempts, analysisId);
	}

	@Test
	void uploadReturnsAcceptedRightAwayAndAnalysisFinishesLater() throws Exception {
		analyzer.enqueue(Duration.ofMillis(800), one("느린 분석"));

		long started = System.currentTimeMillis();
		var accepted = upload(user)
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.status").value("pending"))
				.andExpect(jsonPath("$.candidates").isEmpty())
				.andReturn();
		assertThat(System.currentTimeMillis() - started).isLessThan(800);
		long analysisId = idOf(accepted);
		assertThat(accepted.getResponse().getHeader("Location")).isEqualTo("/api/analyses/" + analysisId);

		String during = com.jayway.jsonpath.JsonPath.read(get(user, analysisId).andReturn().getResponse()
				.getContentAsString(), "$.status");
		assertThat(during).isIn("pending", "processing");

		assertThat(AnalysisWait.untilFinished(mvc, user.bearer(), analysisId)).isEqualTo("needs_review");
		get(user, analysisId)
				.andExpect(jsonPath("$.attemptCount").value(1))
				.andExpect(jsonPath("$.candidates[0].title").value("느린 분석"));
	}

	@Test
	void otherUsersGet404BeforeAndAfterAnalysisFinishes() throws Exception {
		TestUser stranger = signupAndLogin("stranger");
		analyzer.enqueue(Duration.ofMillis(300), one("내 분석"));
		long analysisId = idOf(upload(user).andExpect(status().isAccepted()).andReturn());

		get(stranger, analysisId).andExpect(status().isNotFound());
		AnalysisWait.untilFinished(mvc, user.bearer(), analysisId);
		get(stranger, analysisId)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ANALYSIS_NOT_FOUND"));
	}

	@Test
	void stuckProcessingAnalysisIsPutBackAndFinished() throws Exception {
		analyzer.enqueueFailure("AI_TIMEOUT");
		long analysisId = idOf(upload(user).andReturn());
		AnalysisWait.untilFinished(mvc, user.bearer(), analysisId);
		// 서버가 처리 도중 꺼진 것처럼 오래된 processing으로 만든다.
		forceStatus(analysisId, "processing", Instant.now().minus(Duration.ofMinutes(10)), 1);

		analyzer.enqueue(one("복구된 분석"));
		assertThat(worker.recoverStuck(Instant.now())).isEqualTo(1);

		assertThat(AnalysisWait.untilFinished(mvc, user.bearer(), analysisId)).isEqualTo("needs_review");
		get(user, analysisId)
				.andExpect(jsonPath("$.attemptCount").value(2))
				.andExpect(jsonPath("$.candidates[0].title").value("복구된 분석"));
	}

	@Test
	void recentProcessingIsLeftAloneAndExhaustedOneFails() throws Exception {
		analyzer.enqueueFailure("AI_TIMEOUT");
		long recent = idOf(upload(user).andReturn());
		analyzer.enqueueFailure("AI_TIMEOUT");
		long exhausted = idOf(upload(user).andReturn());
		AnalysisWait.untilFinished(mvc, user.bearer(), recent);
		AnalysisWait.untilFinished(mvc, user.bearer(), exhausted);
		forceStatus(recent, "processing", Instant.now().minus(Duration.ofMinutes(1)), 1);
		forceStatus(exhausted, "processing", Instant.now().minus(Duration.ofMinutes(10)), 5);

		worker.recoverStuck(Instant.now());

		get(user, recent).andExpect(jsonPath("$.status").value("processing"));
		get(user, exhausted)
				.andExpect(jsonPath("$.status").value("failed"))
				.andExpect(jsonPath("$.failureCode").value("ANALYSIS_TIMEOUT"));
	}

	@Test
	void lateResultOfReplacedProcessingIsDiscarded() throws Exception {
		analyzer.enqueue(Duration.ofMillis(1500), one("늦게 끝난 옛 처리"));
		long analysisId = idOf(upload(user).andReturn());
		long deadline = System.currentTimeMillis() + 5000;
		while (!jdbc.queryForObject("select status from image_analyses where id = ?", String.class, analysisId)
				.equals("processing")) {
			assertThat(System.currentTimeMillis()).isLessThan(deadline);
			Thread.sleep(10);
		}
		// 처리 중인 것을 멈춘 것으로 보고 복구하면 새 처리가 먼저 끝난다.
		jdbc.update("update image_analyses set processing_started_at = ? where id = ?",
				Timestamp.from(Instant.now().minus(Duration.ofMinutes(10))), analysisId);
		analyzer.enqueue(one("새 처리"));
		worker.recoverStuck(Instant.now());
		AnalysisWait.untilFinished(mvc, user.bearer(), analysisId);

		Thread.sleep(1800);
		get(user, analysisId)
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.candidates.length()").value(1))
				.andExpect(jsonPath("$.candidates[0].title").value("새 처리"));
	}

	@Test
	void pendingAnalysisMissedByExecutorIsPickedUpByPolling() throws Exception {
		analyzer.enqueueFailure("AI_TIMEOUT");
		long analysisId = idOf(upload(user).andReturn());
		AnalysisWait.untilFinished(mvc, user.bearer(), analysisId);
		forceStatus(analysisId, "pending", null, 1);

		analyzer.enqueue(one("폴링으로 처리"));
		assertThat(worker.pollPending()).isGreaterThanOrEqualTo(1);

		assertThat(AnalysisWait.untilFinished(mvc, user.bearer(), analysisId)).isEqualTo("needs_review");
	}

	@Test
	void fakeAnalyzerCanBeConfiguredToBeSlowAndFailFirst() {
		FakeScheduleAnalyzer configured = new FakeScheduleAnalyzer(Duration.ofMillis(200), 2);
		AnalysisInput input = new AnalysisInput(PNG, "image/png", "Asia/Seoul");

		long started = System.currentTimeMillis();
		assertThatThrownBy(() -> configured.analyze(input)).isInstanceOf(ScheduleAnalysisException.class);
		assertThat(System.currentTimeMillis() - started).isGreaterThanOrEqualTo(200);
		assertThatThrownBy(() -> configured.analyze(input)).isInstanceOf(ScheduleAnalysisException.class);
		assertThat(configured.analyze(input).candidates()).isNotEmpty();
	}

}
