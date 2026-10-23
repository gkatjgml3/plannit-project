package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class DataIntegrityTest extends ApiTestSupport {

	private static final String START = "2026-11-10T09:00:00+09:00";
	private static final String END = "2026-11-10T10:00:00+09:00";

	@Test
	void eventUserSettingsRequiresExistingUser() throws Exception {
		TestUser user = signupAndLogin("fk");
		long eventId = idOf(mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("외래 키", START, END)))
				.andReturn());

		assertThatThrownBy(() -> jdbc.update(
				"insert into event_user_settings (event_id, user_id, hidden) values (?, ?, false)", eventId, 99_999_999L))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void sameKeyWithSameContentReturnsOriginalEvent() throws Exception {
		TestUser user = signupAndLogin("same");
		MvcResult first = mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-same")
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("같은 요청", START, END)))
				.andExpect(status().isCreated())
				.andReturn();
		// 같은 시각을 UTC 표기로 보낸 재시도도 같은 요청으로 본다.
		MvcResult retry = mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-same")
				.contentType(MediaType.APPLICATION_JSON)
				.content(eventJson("같은 요청", "2026-11-10T00:00:00Z", "2026-11-10T01:00:00Z")))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(idOf(retry)).isEqualTo(idOf(first));
		assertThat(countEvents(user)).isEqualTo(1);
	}

	@Test
	void sameKeyWithDifferentContentIsRejected() throws Exception {
		TestUser user = signupAndLogin("reuse");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-reuse")
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("처음 내용", START, END)))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-reuse")
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("다른 내용", START, END)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-reuse")
				.contentType(MediaType.APPLICATION_JSON)
				.content(eventJson("처음 내용", START, "2026-11-10T11:00:00+09:00")))
				.andExpect(status().isConflict());

		assertThat(countEvents(user)).isEqualTo(1);
		assertThat(jdbc.queryForObject("select title from events where created_by = ?", String.class, user.id()))
				.isEqualTo("처음 내용");
	}

	@Test
	void sameKeyForTeamEventAfterPersonalEventIsRejected() throws Exception {
		TestUser user = signupAndLogin("scope");
		long teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"스터디\"}"))
				.andReturn());
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-scope")
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("같은 제목", START, END)))
				.andExpect(status().isCreated());

		// 내용이 같아도 개인 일정에 쓴 키를 팀 일정에 쓰면 다른 요청이다.
		mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", user.bearer())
				.header("Idempotency-Key", "k-scope")
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("같은 제목", START, END)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
	}

	@Test
	void differentUsersMayUseSameKey() throws Exception {
		TestUser first = signupAndLogin("k1");
		TestUser second = signupAndLogin("k2");
		for (TestUser user : new TestUser[] { first, second }) {
			mvc.perform(post("/api/events").header("Authorization", user.bearer())
					.header("Idempotency-Key", "shared-key")
					.contentType(MediaType.APPLICATION_JSON).content(eventJson("각자 일정", START, END)))
					.andExpect(status().isCreated());
		}
	}

	private long countEvents(TestUser user) {
		return jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, user.id());
	}

}
