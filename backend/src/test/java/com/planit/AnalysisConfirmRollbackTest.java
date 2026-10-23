package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jayway.jsonpath.JsonPath;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;
import com.planit.analysis.analyzer.FakeScheduleAnalyzer;
import com.planit.event.Event;
import com.planit.event.EventRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;

/** 검증을 통과한 뒤 일정 저장 도중 DB 오류가 나도, 먼저 저장한 일정까지 모두 되돌려지는지 확인한다. */
class AnalysisConfirmRollbackTest extends ApiTestSupport {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };

	@Autowired
	private FakeScheduleAnalyzer analyzer;

	@MockitoSpyBean
	private EventRepository events;

	@Test
	void databaseFailureDuringConfirmRollsBackEverything() throws Exception {
		TestUser user = signupAndLogin("rollback");
		analyzer.reset();
		analyzer.enqueue(new AnalyzerResult(List.of(
				new CandidateDraft("첫째", "2026-12-01", "09:00", null, "10:00", null, null, List.of(), null, false,
						null, List.of(), List.of()),
				new CandidateDraft("둘째", "2026-12-02", "09:00", null, "10:00", null, null, List.of(), null, false,
						null, List.of(), List.of())),
				List.of()));
		MvcResult created = mvc.perform(multipart("/api/analyses")
				.file(new MockMultipartFile("image", "n.png", "image/png", PNG))
				.header("Authorization", user.bearer()))
				.andExpect(status().isAccepted())
				.andReturn();
		long analysisId = idOf(created);
		AnalysisWait.untilFinished(mvc, user.bearer(), analysisId);
		created = mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer())).andReturn();
		List<Number> ids = JsonPath.read(created.getResponse().getContentAsString(), "$.candidates[*].id");

		// 첫 번째 일정 저장은 실제 저장소로 넘기고, 두 번째 저장에서 DB 오류를 낸다.
		// 스파이의 기본 동작(실제 저장소로 위임)을 꺼내 첫 번째 호출에 쓴다.
		Answer<?> realCall = Mockito.mockingDetails(events).getMockCreationSettings().getDefaultAnswer();
		AtomicInteger saves = new AtomicInteger();
		doAnswer(invocation -> {
			if (saves.getAndIncrement() == 0) {
				return realCall.answer(invocation);
			}
			throw new DataIntegrityViolationException("테스트용 저장 실패");
		}).when(events).saveAndFlush(any(Event.class));
		Exception thrown = null;
		try {
			mvc.perform(post("/api/analyses/{id}/confirm", analysisId).header("Authorization", user.bearer())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"candidateIds\":[%d,%d]}".formatted(ids.get(0).longValue(), ids.get(1).longValue())));
		}
		catch (Exception e) {
			// 처리되지 않은 예외가 MockMvc 밖으로 나온다. 확인할 것은 DB 상태다.
			thrown = e;
		}
		assertThat(saves.get()).isEqualTo(2);
		assertThat(thrown).hasRootCauseInstanceOf(DataIntegrityViolationException.class)
				.rootCause().hasMessage("테스트용 저장 실패");

		assertThat(jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, user.id()))
				.isZero();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.candidates[0].eventId").doesNotExist())
				.andExpect(jsonPath("$.candidates[1].eventId").doesNotExist());
	}

}
