package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

class RecurrenceAndChecklistTest extends ApiTestSupport {

	private static String event(String title, String startsAt, String endsAt, String timezone, String extra) {
		return """
				{"title":"%s","startsAt":"%s","endsAt":"%s","timezone":"%s"%s}
				""".formatted(title, startsAt, endsAt, timezone, extra.isEmpty() ? "" : "," + extra);
	}

	private MvcResult create(TestUser user, String body) throws Exception {
		return mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn();
	}

	private ResultActions calendar(TestUser user, String from, String to, String timezone) throws Exception {
		return mvc.perform(get("/api/events").param("from", from).param("to", to).param("timezone", timezone)
				.header("Authorization", user.bearer()));
	}

	private static List<String> startsOf(ResultActions result) throws Exception {
		return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$[*].startsAt");
	}

	// ---------- 준비물 ----------

	@Test
	void checklistCreateReadUpdateDelete() throws Exception {
		TestUser user = signupAndLogin("check");
		MvcResult created = create(user, event("과학 실험", "2026-11-03T10:00:00+09:00", "2026-11-03T11:00:00+09:00",
				"Asia/Seoul", "\"checklist\":[\"실험복\",\" 보안경 \",\"보고서\"]"));
		long id = idOf(created);

		mvc.perform(get("/api/events/{id}", id).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.checklist", hasSize(3)))
				.andExpect(jsonPath("$.checklist[0].content").value("실험복"))
				.andExpect(jsonPath("$.checklist[1].content").value("보안경"))
				.andExpect(jsonPath("$.checklist[2].sortOrder").value(2));

		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("과학 실험", "2026-11-03T10:00:00+09:00", "2026-11-03T11:00:00+09:00", "Asia/Seoul",
						"\"version\":0,\"checklist\":[\"보고서\",\"계산기\"]")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.checklist", hasSize(2)))
				.andExpect(jsonPath("$.checklist[1].content").value("계산기"))
				// 준비물만 바뀌어도 version이 올라간다.
				.andExpect(jsonPath("$.version").value(1));

		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("과학 실험", "2026-11-03T10:00:00+09:00", "2026-11-03T11:00:00+09:00", "Asia/Seoul",
						"\"version\":1")))
				.andExpect(jsonPath("$.checklist", hasSize(0)));
		assertThat(jdbc.queryForObject("select count(*) from event_checklist_items where event_id = ?", Long.class, id))
				.isZero();

		calendar(user, "2026-11-01", "2026-11-30", "Asia/Seoul").andExpect(jsonPath("$[0].checklist", hasSize(0)));
	}

	@Test
	void checklistIsPartOfIdempotencyFingerprint() throws Exception {
		TestUser user = signupAndLogin("checkkey");
		String base = "\"checklist\":[\"%s\"]";
		mvc.perform(post("/api/events").header("Authorization", user.bearer()).header("Idempotency-Key", "ck")
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("시험", "2026-11-03T10:00:00+09:00", "2026-11-03T11:00:00+09:00", "Asia/Seoul",
						base.formatted("필통"))))
				.andExpect(status().isCreated());
		mvc.perform(post("/api/events").header("Authorization", user.bearer()).header("Idempotency-Key", "ck")
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("시험", "2026-11-03T10:00:00+09:00", "2026-11-03T11:00:00+09:00", "Asia/Seoul",
						base.formatted("계산기"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
	}

	// ---------- 반복 펼치기 ----------

	@Test
	void weeklyRecurrenceExpandsOnSelectedDaysWithoutCopyingRows() throws Exception {
		TestUser user = signupAndLogin("weekly");
		long id = idOf(create(user, event("수학 학원", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00",
				"Asia/Seoul", "\"recurrence\":{\"frequency\":\"WEEKLY\",\"byDays\":[\"MO\",\"WE\"]}")));

		ResultActions november = calendar(user, "2026-11-01", "2026-11-30", "Asia/Seoul")
				.andExpect(jsonPath("$", hasSize(9)))
				.andExpect(jsonPath("$[*].id", everyItem(org.hamcrest.Matchers.is((int) id))))
				.andExpect(jsonPath("$[1].occurrenceStartsAt").value("2026-11-04T09:00:00+09:00"))
				.andExpect(jsonPath("$[0].recurrence.frequency").value("WEEKLY"))
				.andExpect(jsonPath("$[0].recurrence.byDays[1]").value("WE"));
		assertThat(startsOf(november)).containsExactly("2026-11-02T09:00:00+09:00", "2026-11-04T09:00:00+09:00",
				"2026-11-09T09:00:00+09:00", "2026-11-11T09:00:00+09:00", "2026-11-16T09:00:00+09:00",
				"2026-11-18T09:00:00+09:00", "2026-11-23T09:00:00+09:00", "2026-11-25T09:00:00+09:00",
				"2026-11-30T09:00:00+09:00");
		assertThat(jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, user.id()))
				.isEqualTo(1);

		// 단건 조회는 원본 일정을 돌려준다.
		mvc.perform(get("/api/events/{id}", id).header("Authorization", user.bearer()))
				.andExpect(jsonPath("$.startsAt").value("2026-11-02T09:00:00+09:00"))
				.andExpect(jsonPath("$.occurrenceStartsAt").doesNotExist());
	}

	@Test
	void expansionRespectsPeriodBoundaries() throws Exception {
		TestUser user = signupAndLogin("bound");
		create(user, event("월수 수업", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"WEEKLY\",\"byDays\":[\"MO\",\"WE\"]}"));
		assertThat(startsOf(calendar(user, "2026-11-10", "2026-11-16", "Asia/Seoul")))
				.containsExactly("2026-11-11T09:00:00+09:00", "2026-11-16T09:00:00+09:00");

		// 자정을 넘는 회차는 다음 날 조회에도 겹치는 만큼 나온다.
		TestUser night = signupAndLogin("night");
		create(night, event("야간 자습", "2026-11-01T23:00:00+09:00", "2026-11-02T01:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"DAILY\"}"));
		assertThat(startsOf(calendar(night, "2026-11-05", "2026-11-05", "Asia/Seoul")))
				.containsExactly("2026-11-04T23:00:00+09:00", "2026-11-05T23:00:00+09:00");
		// 원본 시작 전 기간에는 회차가 없다.
		calendar(night, "2026-10-01", "2026-10-31", "Asia/Seoul").andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void untilAndIntervalLimitOccurrences() throws Exception {
		TestUser user = signupAndLogin("until");
		create(user, event("격일 운동", "2026-11-01T07:00:00+09:00", "2026-11-01T08:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"DAILY\",\"interval\":2,\"until\":\"2026-11-07\"}"));
		assertThat(startsOf(calendar(user, "2026-10-01", "2026-12-31", "Asia/Seoul"))).containsExactly(
				"2026-11-01T07:00:00+09:00", "2026-11-03T07:00:00+09:00", "2026-11-05T07:00:00+09:00",
				"2026-11-07T07:00:00+09:00");
	}

	@Test
	void countLimitsOccurrencesEvenOutsideWindow() throws Exception {
		TestUser user = signupAndLogin("count");
		create(user, event("3회 특강", "2026-11-03T16:00:00+09:00", "2026-11-03T17:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"WEEKLY\",\"count\":3}"));
		assertThat(startsOf(calendar(user, "2026-11-01", "2026-12-31", "Asia/Seoul"))).containsExactly(
				"2026-11-03T16:00:00+09:00", "2026-11-10T16:00:00+09:00", "2026-11-17T16:00:00+09:00");
		// 3회가 끝난 뒤 기간에는 회차가 없다.
		calendar(user, "2026-11-18", "2026-12-31", "Asia/Seoul").andExpect(jsonPath("$", hasSize(0)));
		// 기간이 중간부터 시작해도 앞 회차를 세어 count를 지킨다.
		assertThat(startsOf(calendar(user, "2026-11-09", "2026-12-31", "Asia/Seoul")))
				.containsExactly("2026-11-10T16:00:00+09:00", "2026-11-17T16:00:00+09:00");
	}

	@Test
	void monthlySkipsMonthsWithoutThatDay() throws Exception {
		TestUser user = signupAndLogin("monthly");
		create(user, event("말일 정산", "2026-01-31T18:00:00+09:00", "2026-01-31T19:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"MONTHLY\",\"count\":3}"));
		assertThat(startsOf(calendar(user, "2026-01-01", "2026-12-31", "Asia/Seoul"))).containsExactly(
				"2026-01-31T18:00:00+09:00", "2026-03-31T18:00:00+09:00", "2026-05-31T18:00:00+09:00");
	}

	@Test
	void localTimeIsKeptAcrossTimezonesAndDaylightSaving() throws Exception {
		TestUser user = signupAndLogin("tz");
		create(user, event("월요 회의(KST)", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00", "Asia/Seoul",
				"\"recurrence\":{\"frequency\":\"WEEKLY\",\"count\":3}"));
		// UTC 기준으로 조회해도 응답은 일정의 timezone(KST) 09:00을 유지한다.
		assertThat(startsOf(calendar(user, "2026-11-01", "2026-11-30", "UTC"))).containsExactly(
				"2026-11-02T09:00:00+09:00", "2026-11-09T09:00:00+09:00", "2026-11-16T09:00:00+09:00");

		// 서머타임이 끝나는 주(2026-11-01)를 지나도 뉴욕 현지 09:00이 유지되고 UTC 오프셋만 바뀐다.
		TestUser nyUser = signupAndLogin("ny");
		create(nyUser, event("NY standup", "2026-10-26T09:00:00-04:00", "2026-10-26T09:30:00-04:00",
				"America/New_York", "\"recurrence\":{\"frequency\":\"WEEKLY\",\"count\":2}"));
		assertThat(startsOf(calendar(nyUser, "2026-10-20", "2026-11-10", "UTC")))
				.containsExactly("2026-10-26T09:00:00-04:00", "2026-11-02T09:00:00-05:00");
	}

	@Test
	void teamRecurringEventShowsAsOccurrencesForMembers() throws Exception {
		TestUser owner = signupAndLogin("owner");
		TestUser member = signupAndLogin("member");
		long teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"독서 동아리\"}")).andReturn());
		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
				.andExpect(status().isCreated());

		mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("독서 모임", "2026-11-06T17:00:00+09:00", "2026-11-06T18:00:00+09:00", "Asia/Seoul",
						"\"recurrence\":{\"frequency\":\"WEEKLY\",\"until\":\"2026-11-27\"},\"checklist\":[\"책\"]")))
				.andExpect(status().isCreated());

		calendar(member, "2026-11-01", "2026-11-30", "Asia/Seoul")
				.andExpect(jsonPath("$", hasSize(4)))
				.andExpect(jsonPath("$[*].teamId", everyItem(org.hamcrest.Matchers.is((int) teamId))))
				.andExpect(jsonPath("$[3].startsAt").value("2026-11-27T17:00:00+09:00"))
				.andExpect(jsonPath("$[0].checklist[0].content").value("책"));
		assertThat(jdbc.queryForObject("select count(*) from events where team_id = ?", Long.class, teamId))
				.isEqualTo(1);
	}

	@Test
	void recurrenceCanBeChangedAndRemoved() throws Exception {
		TestUser user = signupAndLogin("change");
		long id = idOf(create(user, event("반복 변경", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00",
				"Asia/Seoul", "\"recurrence\":{\"frequency\":\"DAILY\",\"count\":5}")));
		calendar(user, "2026-11-01", "2026-11-30", "Asia/Seoul").andExpect(jsonPath("$", hasSize(5)));

		mvc.perform(put("/api/events/{id}", id).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content(event("반복 변경", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00", "Asia/Seoul",
						"\"version\":0")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.recurrence").doesNotExist());
		calendar(user, "2026-11-01", "2026-11-30", "Asia/Seoul").andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void invalidRecurrenceRulesAreRejected() throws Exception {
		TestUser user = signupAndLogin("badrule");
		// 2026-11-02는 월요일
		String[] invalid = {
				"{\"frequency\":\"YEARLY\"}",
				"{}",
				"{\"frequency\":\"DAILY\",\"interval\":0}",
				"{\"frequency\":\"DAILY\",\"interval\":366}",
				"{\"frequency\":\"DAILY\",\"until\":\"2026-12-01\",\"count\":3}",
				"{\"frequency\":\"DAILY\",\"byDays\":[\"MO\"]}",
				"{\"frequency\":\"WEEKLY\",\"byDays\":[\"TU\",\"WE\"]}",
				"{\"frequency\":\"WEEKLY\",\"byDays\":[\"MO\",\"XX\"]}",
				"{\"frequency\":\"WEEKLY\",\"byDays\":[\"MO\",\"MO\"]}",
				"{\"frequency\":\"DAILY\",\"until\":\"2026-11-01\"}",
				"{\"frequency\":\"DAILY\",\"until\":\"2026-02-30\"}",
				"{\"frequency\":\"DAILY\",\"count\":0}",
				"{\"frequency\":\"DAILY\",\"count\":1001}" };
		for (String rule : invalid) {
			mvc.perform(post("/api/events").header("Authorization", user.bearer())
					.contentType(MediaType.APPLICATION_JSON)
					.content(event("잘못된 반복", "2026-11-02T09:00:00+09:00", "2026-11-02T10:00:00+09:00", "Asia/Seoul",
							"\"recurrence\":" + rule)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("INVALID_RECURRENCE"));
		}
		assertThat(jdbc.queryForObject("select count(*) from events where created_by = ?", Long.class, user.id()))
				.isZero();
	}

	@Test
	void tooLongCalendarRangeIsRejected() throws Exception {
		TestUser user = signupAndLogin("range");
		// 양 끝을 포함해 367일은 거부, 366일까지는 허용
		calendar(user, "2026-01-01", "2027-01-02", "Asia/Seoul")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("DATE_RANGE_TOO_LONG"));
		calendar(user, "2026-01-01", "2027-01-01", "Asia/Seoul").andExpect(status().isOk());
	}

}
