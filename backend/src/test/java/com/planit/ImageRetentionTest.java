package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.planit.analysis.ImageRetentionService;
import com.planit.analysis.analyzer.AnalyzerResult;
import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;
import com.planit.analysis.analyzer.FakeScheduleAnalyzer;
import com.planit.analysis.storage.InMemoryImageStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class ImageRetentionTest extends ApiTestSupport {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13 };

	@Autowired
	private FakeScheduleAnalyzer analyzer;

	@Autowired
	private ImageRetentionService retention;

	@MockitoSpyBean
	private InMemoryImageStorage storage;

	private TestUser user;

	@BeforeEach
	void setUp() throws Exception {
		analyzer.reset();
		user = signupAndLogin("retention");
	}

	private long upload() throws Exception {
		long id = idOf(mvc.perform(multipart("/api/analyses")
				.file(new MockMultipartFile("image", "n.png", "image/png", PNG))
				.header("Authorization", user.bearer())).andReturn());
		AnalysisWait.untilFinished(mvc, user.bearer(), id);
		return id;
	}

	private long analyzeOne(String title) throws Exception {
		analyzer.enqueue(new AnalyzerResult(List.of(new CandidateDraft(title, "2026-12-01", "09:00", null, "10:00",
				null, null, List.of(), null, false, null, List.of(), List.of())), List.of()));
		return upload();
	}

	private String imageKey(long analysisId) {
		return jdbc.queryForObject("select image_key from image_analyses where id = ?", String.class, analysisId);
	}

	private void confirmAll(long analysisId) throws Exception {
		String body = mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andReturn().getResponse().getContentAsString();
		List<Number> ids = JsonPath.read(body, "$.candidates[*].id");
		mvc.perform(post("/api/analyses/{id}/confirm", analysisId).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"candidateIds\":[%d]}".formatted(ids.get(0).longValue())))
				.andExpect(status().isOk());
	}

	@Test
	void imageIsDeletedRightAfterConfirmButRecordsStay() throws Exception {
		long analysisId = analyzeOne("확정하면 삭제");
		String key = imageKey(analysisId);
		assertThat(storage.contains(key)).isTrue();

		confirmAll(analysisId);

		assertThat(storage.contains(key)).isFalse();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("confirmed"))
				.andExpect(jsonPath("$.imageDeleted").value(true))
				.andExpect(jsonPath("$.imageDeletedAt").isNotEmpty())
				.andExpect(jsonPath("$.candidates", hasSize(1)))
				.andExpect(jsonPath("$.candidates[0].title").value("확정하면 삭제"));
	}

	@Test
	void unconfirmedImageIsDeletedAfterRetentionPeriod() throws Exception {
		long old = analyzeOne("오래된 분석");
		long fresh = analyzeOne("새 분석");
		analyzer.enqueueFailure("AI_TIMEOUT");
		long oldFailed = upload();
		for (long id : new long[] { old, oldFailed }) {
			jdbc.update("update image_analyses set created_at = ? where id = ?",
					Timestamp.from(Instant.now().minus(Duration.ofDays(8))), id);
		}

		retention.cleanup(Instant.now());

		assertThat(storage.contains(imageKey(old))).isFalse();
		assertThat(storage.contains(imageKey(oldFailed))).isFalse();
		assertThat(storage.contains(imageKey(fresh))).isTrue();
		mvc.perform(get("/api/analyses/{id}", old).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("needs_review"))
				.andExpect(jsonPath("$.imageDeleted").value(true))
				.andExpect(jsonPath("$.candidates[0].title").value("오래된 분석"));
		mvc.perform(get("/api/analyses/{id}", fresh).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.imageDeleted").value(false));
		// 이미지가 지워진 실패 분석은 다시 시도할 수 없다.
		mvc.perform(post("/api/analyses/{id}/retry", oldFailed).header("Authorization", user.bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IMAGE_DELETED"));
		// 이미지가 지워져도 남은 후보로 확정할 수는 있다.
		confirmAll(old);
	}

	@Test
	void failedDeletionIsRetriedByNextCleanup() throws Exception {
		long analysisId = analyzeOne("삭제 실패");
		String key = imageKey(analysisId);
		doThrow(new IllegalStateException("저장소 오류")).when(storage).delete(anyString());

		confirmAll(analysisId);

		assertThat(storage.contains(key)).isTrue();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.status").value("confirmed"))
				.andExpect(jsonPath("$.imageDeleted").value(false));

		// 다음 정리 때도 실패하면 그대로 남는다.
		retention.cleanup(Instant.now());
		assertThat(storage.contains(key)).isTrue();

		doCallRealMethod().when(storage).delete(anyString());
		retention.cleanup(Instant.now());
		assertThat(storage.contains(key)).isFalse();
		mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.imageDeleted").value(true));
	}

}
