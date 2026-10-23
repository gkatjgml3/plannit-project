package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TeamApiTest extends ApiTestSupport {

	private static final String START = "2026-11-05T15:00:00+09:00";
	private static final String END = "2026-11-05T17:00:00+09:00";

	private TestUser owner;
	private TestUser member;
	private TestUser outsider;
	private long teamId;

	@BeforeEach
	void setUpTeam() throws Exception {
		owner = signupAndLogin("owner");
		member = signupAndLogin("member");
		outsider = signupAndLogin("outsider");

		teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"1학년 3반\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.myRole").value("OWNER"))
				.andReturn());

		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("MEMBER"));
	}

	private long createTeamEvent(TestUser by, String title) throws Exception {
		return idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", by.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson(title, START, END)))
				.andExpect(status().isCreated())
				.andReturn());
	}

	@Test
	void teamMemberSeesTeamEventInCalendar() throws Exception {
		long eventId = createTeamEvent(owner, "조별 과제 발표");

		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", member.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(eventId))
				.andExpect(jsonPath("$[0].teamId").value(teamId));
		mvc.perform(get("/api/events/{id}", eventId).header("Authorization", member.bearer()))
				.andExpect(status().isOk());
	}

	@Test
	void outsiderCannotSeeTeamEvents() throws Exception {
		long eventId = createTeamEvent(owner, "조별 과제 발표");

		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", outsider.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/events/{id}", eventId).header("Authorization", outsider.bearer()))
				.andExpect(status().isNotFound());
	}

	@Test
	void outsiderIsForbiddenFromTeamApis() throws Exception {
		mvc.perform(get("/api/teams/{teamId}/members", teamId).header("Authorization", outsider.bearer()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("NOT_TEAM_MEMBER"));
		mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", outsider.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("끼어들기", START, END)))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", outsider.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(outsider.email())))
				.andExpect(status().isForbidden());
	}

	@Test
	void onlyOwnerCanAddMembers() throws Exception {
		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", member.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(outsider.email())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("NOT_TEAM_OWNER"));
	}

	@Test
	void teamEventIsStoredOnceNotCopiedPerMember() throws Exception {
		long eventId = createTeamEvent(member, "동아리 회의");

		// 두 팀원이 모두 캘린더를 조회해도 events 테이블에는 원본 한 행만 있다.
		for (TestUser user : new TestUser[] { owner, member }) {
			mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
					.header("Authorization", user.bearer()))
					.andExpect(jsonPath("$[0].id").value(eventId));
		}
		assertThat(jdbc.queryForObject("select count(*) from events where team_id = ?", Long.class, teamId))
				.isEqualTo(1);
		assertThat(jdbc.queryForObject("select count(*) from events where team_id is null and created_by in (?, ?)",
				Long.class, owner.id(), member.id()))
				.isZero();
	}

	@Test
	void hidingTeamEventOnlyAffectsThatMember() throws Exception {
		long eventId = createTeamEvent(owner, "학급 회의");

		mvc.perform(put("/api/events/{id}/settings", eventId).header("Authorization", member.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"hidden\":true,\"personalMemo\":\"나는 불참\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hidden").value(true));

		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", member.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", owner.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].personalMemo").doesNotExist());
	}

	@Test
	void memberCannotEditOthersTeamEventButOwnerCan() throws Exception {
		long eventId = createTeamEvent(member, "체육대회 연습");
		long ownersEvent = createTeamEvent(owner, "담임 공지");
		String update = """
				{"title":"바뀐 제목","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
				""".formatted(START, END);

		mvc.perform(put("/api/events/{id}", ownersEvent).header("Authorization", member.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update))
				.andExpect(status().isForbidden());
		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update))
				.andExpect(status().isOk());
	}

	@Test
	void listsMyTeamsAndMembers() throws Exception {
		mvc.perform(get("/api/teams").header("Authorization", member.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name").value("1학년 3반"))
				.andExpect(jsonPath("$[0].myRole").value("MEMBER"));
		mvc.perform(get("/api/teams/{teamId}/members", teamId).header("Authorization", member.bearer()))
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/teams").header("Authorization", outsider.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
	}

}
