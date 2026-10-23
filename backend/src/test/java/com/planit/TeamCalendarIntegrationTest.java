package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/**
 * 팀장 O, 팀원 A·B, 팀 밖 사용자 X.
 * A에게 개인 일정 하나(personalA)와 A가 만든 팀 일정 하나(teamEvent)가 있다.
 */
class TeamCalendarIntegrationTest extends ApiTestSupport {

	private static final String START = "2026-11-12T15:00:00+09:00";
	private static final String END = "2026-11-12T16:00:00+09:00";

	private TestUser owner;
	private TestUser memberA;
	private TestUser memberB;
	private TestUser outsider;
	private long teamId;
	private long personalA;
	private long teamEvent;

	@BeforeEach
	void setUp() throws Exception {
		owner = signupAndLogin("o");
		memberA = signupAndLogin("a");
		memberB = signupAndLogin("b");
		outsider = signupAndLogin("x");

		teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"과학 동아리\"}"))
				.andReturn());
		for (TestUser member : new TestUser[] { memberA, memberB }) {
			mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", owner.bearer())
					.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
					.andExpect(status().isCreated());
		}

		personalA = idOf(mvc.perform(post("/api/events").header("Authorization", memberA.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("A 개인 학원", START, END)))
				.andExpect(status().isCreated())
				.andReturn());
		teamEvent = idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", memberA.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("동아리 실험", START, END)))
				.andExpect(status().isCreated())
				.andReturn());
	}

	private ResultActions calendarOf(TestUser user) throws Exception {
		return mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", user.bearer()));
	}

	@Test
	void memberAseesPersonalAndTeamEvents() throws Exception {
		calendarOf(memberA)
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].id").value(containsInAnyOrder((int) personalA, (int) teamEvent)));
	}

	@Test
	void memberBseesTeamEventButNotAsPersonalEvent() throws Exception {
		calendarOf(memberB)
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(teamEvent))
				.andExpect(jsonPath("$[0].teamId").value(teamId));
		mvc.perform(get("/api/events/{id}", personalA).header("Authorization", memberB.bearer()))
				.andExpect(status().isNotFound());
	}

	@Test
	void teamEventIsStoredOnlyOnce() throws Exception {
		calendarOf(owner).andExpect(jsonPath("$", hasSize(1)));
		calendarOf(memberA).andExpect(jsonPath("$", hasSize(2)));
		calendarOf(memberB).andExpect(jsonPath("$", hasSize(1)));

		assertThat(jdbc.queryForObject("select count(*) from events where team_id = ?", Long.class, teamId))
				.isEqualTo(1);
		assertThat(jdbc.queryForObject("select count(*) from events where title = ? and created_by in (?, ?, ?)",
				Long.class, "동아리 실험", owner.id(), memberA.id(), memberB.id()))
				.isEqualTo(1);
	}

	@Test
	void hidingTeamEventOnlyHidesItForThatMember() throws Exception {
		mvc.perform(put("/api/events/{id}/settings", teamEvent).header("Authorization", memberA.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"hidden\":true}"))
				.andExpect(status().isOk());

		calendarOf(memberA)
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(personalA));
		calendarOf(memberB).andExpect(jsonPath("$[0].id").value(teamEvent));
		calendarOf(owner).andExpect(jsonPath("$[0].id").value(teamEvent));
		assertThat(jdbc.queryForObject("select count(*) from events where id = ? and deleted_at is null", Long.class,
				teamEvent)).isEqualTo(1);
	}

	@Test
	void outsiderCannotLearnTeamEventExists() throws Exception {
		calendarOf(outsider).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/events/{id}", teamEvent).header("Authorization", outsider.bearer()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
		// 존재하지 않는 id와 같은 응답이어야 한다.
		mvc.perform(get("/api/events/{id}", 99_999_999L).header("Authorization", outsider.bearer()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
		mvc.perform(put("/api/events/{id}/settings", teamEvent).header("Authorization", outsider.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"hidden\":true}"))
				.andExpect(status().isNotFound());
		mvc.perform(delete("/api/events/{id}", teamEvent).header("Authorization", outsider.bearer()))
				.andExpect(status().isNotFound());
	}

	@Test
	void teamEventEditPolicy() throws Exception {
		String update = """
				{"title":"동아리 실험 (변경)","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":%d}
				""";
		// 다른 팀원은 남이 만든 팀 일정을 고치거나 지울 수 없다.
		mvc.perform(put("/api/events/{id}", teamEvent).header("Authorization", memberB.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update.formatted(START, END, 0)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("NOT_EVENT_EDITOR"));
		mvc.perform(delete("/api/events/{id}", teamEvent).header("Authorization", memberB.bearer()))
				.andExpect(status().isForbidden());
		// 만든 사람은 고칠 수 있다.
		mvc.perform(put("/api/events/{id}", teamEvent).header("Authorization", memberA.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update.formatted(START, END, 0)))
				.andExpect(status().isOk());
		// 팀장도 고칠 수 있다.
		mvc.perform(put("/api/events/{id}", teamEvent).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update.formatted(START, END, 1)))
				.andExpect(status().isOk());
		// 바뀐 내용은 다른 팀원 조회에 바로 보인다.
		calendarOf(memberB).andExpect(jsonPath("$[0].title").value("동아리 실험 (변경)"));
	}

	@Test
	void personalEventEditPolicy() throws Exception {
		String update = """
				{"title":"바꾸기","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
				""".formatted(START, END);
		// 개인 일정은 같은 팀원이나 팀장이라도 볼 수도, 고칠 수도 없다.
		for (TestUser other : new TestUser[] { memberB, owner }) {
			mvc.perform(put("/api/events/{id}", personalA).header("Authorization", other.bearer())
					.contentType(MediaType.APPLICATION_JSON).content(update))
					.andExpect(status().isNotFound());
			mvc.perform(delete("/api/events/{id}", personalA).header("Authorization", other.bearer()))
					.andExpect(status().isNotFound());
		}
		mvc.perform(put("/api/events/{id}", personalA).header("Authorization", memberA.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update))
				.andExpect(status().isOk());
	}

}
