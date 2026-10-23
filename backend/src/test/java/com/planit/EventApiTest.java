package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class EventApiTest extends ApiTestSupport {

	private static final String START = "2026-11-02T09:00:00+09:00";
	private static final String END = "2026-11-02T10:00:00+09:00";

	@Test
	void personalEventCreateReadUpdateDelete() throws Exception {
		TestUser user = signupAndLogin("crud");

		MvcResult created = mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("수학 수행평가", START, END)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.title").value("수학 수행평가"))
				.andExpect(jsonPath("$.teamId").doesNotExist())
				.andExpect(jsonPath("$.startsAt").value("2026-11-02T09:00:00+09:00"))
				.andExpect(jsonPath("$.version").value(0))
				.andReturn();
		long id = idOf(created);

		mvc.perform(get("/api/events/{id}", id).header("Authorization", user.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.location").value("3층 과학실"));

		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", user.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(id));

		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"수학 수행평가 (2차)","startsAt":"2026-11-03T09:00:00+09:00",
						 "endsAt":"2026-11-03T11:00:00+09:00","timezone":"Asia/Seoul","version":0}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("수학 수행평가 (2차)"))
				.andExpect(jsonPath("$.version").value(1));

		mvc.perform(delete("/api/events/{id}", id).header("Authorization", user.bearer()))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/events/{id}", id).header("Authorization", user.bearer()))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", user.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void endBeforeStartIsRejected() throws Exception {
		TestUser user = signupAndLogin("range");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("거꾸로", END, START)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TIME_RANGE"));
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("같은 시각", START, START)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void blankTitleIsRejected() throws Exception {
		TestUser user = signupAndLogin("blank");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("   ", START, END)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	@Test
	void impossibleDateIsRejected() throws Exception {
		TestUser user = signupAndLogin("baddate");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(eventJson("2월 30일", "2026-02-30T09:00:00+09:00", "2026-02-30T10:00:00+09:00")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
	}

	@Test
	void unknownTimezoneIsRejected() throws Exception {
		TestUser user = signupAndLogin("tz");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"시간대","startsAt":"%s","endsAt":"%s","timezone":"Mars/Olympus"}
						""".formatted(START, END)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TIMEZONE"));
	}

	@Test
	void sameIdempotencyKeyCreatesOnlyOneEvent() throws Exception {
		TestUser user = signupAndLogin("idem");
		String key = "req-" + user.id();

		MvcResult first = mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", key)
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("중복 방지", START, END)))
				.andExpect(status().isCreated())
				.andReturn();
		MvcResult second = mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.header("Idempotency-Key", key)
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("중복 방지", START, END)))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(idOf(second)).isEqualTo(idOf(first));
		assertThat(jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, user.id()))
				.isEqualTo(1);
	}

	@Test
	void otherUserCannotSeeOrEditPersonalEvent() throws Exception {
		TestUser owner = signupAndLogin("owner");
		TestUser stranger = signupAndLogin("stranger");
		long id = idOf(mvc.perform(post("/api/events").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("내 일정", START, END)))
				.andReturn());

		mvc.perform(get("/api/events/{id}", id).header("Authorization", stranger.bearer()))
				.andExpect(status().isNotFound());
		mvc.perform(put("/api/events/{id}", id).header("Authorization", stranger.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"뺏기","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
						""".formatted(START, END)))
				.andExpect(status().isNotFound());
		mvc.perform(delete("/api/events/{id}", id).header("Authorization", stranger.bearer()))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", stranger.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void staleVersionIsRejected() throws Exception {
		TestUser user = signupAndLogin("stale");
		long id = idOf(mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("버전", START, END)))
				.andReturn());
		String update = """
				{"title":"수정","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
				""".formatted(START, END);

		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update))
				.andExpect(status().isOk());
		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
	}

	@Test
	void calendarOnlyReturnsEventsInRange() throws Exception {
		TestUser user = signupAndLogin("window");
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(eventJson("11월 말", "2026-11-30T23:00:00+09:00", "2026-11-30T23:30:00+09:00")));
		mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(eventJson("12월 초", "2026-12-01T00:00:00+09:00", "2026-12-01T01:00:00+09:00")));

		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", user.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("11월 말"));
	}

}
