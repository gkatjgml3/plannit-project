package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.planit.notification.NotificationDispatcher;
import com.planit.notification.NotificationSender;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@ExtendWith(OutputCaptureExtension.class)
class NotificationTest extends ApiTestSupport {

	private static final String START = "2026-12-10T10:00:00+09:00";
	private static final String END = "2026-12-10T11:00:00+09:00";

	@Autowired
	private NotificationDispatcher dispatcher;

	@MockitoSpyBean
	private NotificationSender sender;

	private long createEvent(TestUser user, String title) throws Exception {
		return idOf(mvc.perform(post("/api/events").header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson(title, START, END)))
				.andExpect(status().isCreated()).andReturn());
	}

	private void setReminder(TestUser user, long eventId, String reminderAt) throws Exception {
		String value = reminderAt == null ? "null" : "\"" + reminderAt + "\"";
		mvc.perform(put("/api/events/{id}/settings", eventId).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"hidden\":false,\"reminderAt\":%s,\"personalMemo\":\"비밀 메모\"}".formatted(value)))
				.andExpect(status().isOk());
	}

	private List<Map<String, Object>> outbox(long userId, long eventId) {
		return jdbc.queryForList("select id, kind, status, scheduled_at, attempt_count from notification_outbox "
				+ "where user_id = ? and event_id = ? order by id", userId, eventId);
	}

	private static Instant instantOf(Object timestamp) {
		return timestamp instanceof Timestamp ts ? ts.toInstant() : ((java.time.OffsetDateTime) timestamp).toInstant();
	}

	private long createTeam(TestUser owner, TestUser... members) throws Exception {
		long teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"알림 팀\"}")).andReturn());
		for (TestUser member : members) {
			mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", owner.bearer())
					.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
					.andExpect(status().isCreated());
		}
		return teamId;
	}

	// ---------- 일정 알림 생성·변경·취소 ----------

	@Test
	void reminderIsCreatedRescheduledAndCanceled() throws Exception {
		TestUser user = signupAndLogin("remind");
		long eventId = createEvent(user, "수행평가");

		setReminder(user, eventId, "2026-12-10T09:30:00+09:00");
		assertThat(outbox(user.id(), eventId)).singleElement().satisfies(row -> {
			assertThat(row.get("kind")).isEqualTo("EVENT_REMINDER");
			assertThat(row.get("status")).isEqualTo("PENDING");
			assertThat(instantOf(row.get("scheduled_at"))).isEqualTo(Instant.parse("2026-12-10T00:30:00Z"));
		});

		setReminder(user, eventId, "2026-12-09T20:00:00+09:00");
		assertThat(outbox(user.id(), eventId)).singleElement()
				.satisfies(row -> assertThat(instantOf(row.get("scheduled_at"))).isEqualTo(Instant.parse("2026-12-09T11:00:00Z")));

		setReminder(user, eventId, null);
		assertThat(outbox(user.id(), eventId)).singleElement()
				.satisfies(row -> assertThat(row.get("status")).isEqualTo("CANCELED"));
	}

	@Test
	void movingEventMovesReminderByTheSameAmount() throws Exception {
		TestUser user = signupAndLogin("move");
		long eventId = createEvent(user, "시험");
		setReminder(user, eventId, "2026-12-10T09:30:00+09:00");

		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"시험","startsAt":"2026-12-10T14:00:00+09:00","endsAt":"2026-12-10T15:00:00+09:00",
						 "timezone":"Asia/Seoul","version":0}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reminderAt").value("2026-12-10T04:30:00Z"));

		assertThat(outbox(user.id(), eventId)).singleElement().satisfies(row -> {
			assertThat(row.get("status")).isEqualTo("PENDING");
			assertThat(instantOf(row.get("scheduled_at"))).isEqualTo(Instant.parse("2026-12-10T04:30:00Z"));
		});
	}

	@Test
	void deletingEventCancelsReminder() throws Exception {
		TestUser user = signupAndLogin("del");
		long eventId = createEvent(user, "취소될 일정");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");

		mvc.perform(delete("/api/events/{id}", eventId).header("Authorization", user.bearer()))
				.andExpect(status().isNoContent());
		assertThat(outbox(user.id(), eventId)).singleElement()
				.satisfies(row -> assertThat(row.get("status")).isEqualTo("CANCELED"));
	}

	// ---------- 팀 일정 변경 알림 ----------

	@Test
	void teamEventChangesNotifyOtherMembersOnly() throws Exception {
		TestUser owner = signupAndLogin("owner");
		TestUser a = signupAndLogin("a");
		TestUser b = signupAndLogin("b");
		long teamId = createTeam(owner, a, b);

		long eventId = idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", a.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("조별 회의", START, END)))
				.andExpect(status().isCreated()).andReturn());
		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", a.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"조별 회의(장소 변경)","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
						""".formatted(START, END)))
				.andExpect(status().isOk());
		mvc.perform(delete("/api/events/{id}", eventId).header("Authorization", owner.bearer()))
				.andExpect(status().isNoContent());

		assertThat(outbox(a.id(), eventId)).extracting(r -> r.get("kind")).containsExactly("TEAM_EVENT_DELETED");
		assertThat(outbox(b.id(), eventId)).extracting(r -> r.get("kind"))
				.containsExactly("TEAM_EVENT_CREATED", "TEAM_EVENT_UPDATED", "TEAM_EVENT_DELETED");
		assertThat(outbox(owner.id(), eventId)).extracting(r -> r.get("kind"))
				.containsExactly("TEAM_EVENT_CREATED", "TEAM_EVENT_UPDATED");

		// 개인 일정은 팀 알림을 만들지 않는다.
		long personal = createEvent(a, "개인 일정");
		assertThat(jdbc.queryForObject("select count(*) from notification_outbox where event_id = ?", Long.class,
				personal)).isZero();
	}

	@Test
	void memberWhoLeftDoesNotReceiveNotifications() throws Exception {
		TestUser owner = signupAndLogin("owner");
		TestUser leaver = signupAndLogin("leaver");
		TestUser stayer = signupAndLogin("stayer");
		long teamId = createTeam(owner, leaver, stayer);
		long eventId = idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("학급 회의", START, END)))
				.andExpect(status().isCreated()).andReturn());
		setReminder(leaver, eventId, "2026-12-10T09:00:00+09:00");

		mvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, leaver.id())
				.header("Authorization", leaver.bearer()))
				.andExpect(status().isNoContent());

		// 떠나기 전에 쌓인 대기 알림(생성 알림, 일정 알림)은 모두 취소된다.
		assertThat(outbox(leaver.id(), eventId)).extracting(r -> r.get("status")).containsOnly("CANCELED");

		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"학급 회의(변경)","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":0}
						""".formatted(START, END)))
				.andExpect(status().isOk());

		assertThat(outbox(leaver.id(), eventId)).extracting(r -> r.get("kind")).doesNotContain("TEAM_EVENT_UPDATED");
		assertThat(outbox(stayer.id(), eventId)).extracting(r -> r.get("kind"))
				.containsExactly("TEAM_EVENT_CREATED", "TEAM_EVENT_UPDATED");
		mvc.perform(get("/api/notifications").header("Authorization", leaver.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void teamMembershipRemovalRules() throws Exception {
		TestUser owner = signupAndLogin("owner");
		TestUser a = signupAndLogin("a");
		TestUser b = signupAndLogin("b");
		long teamId = createTeam(owner, a, b);

		mvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, b.id()).header("Authorization", a.bearer()))
				.andExpect(status().isForbidden());
		mvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, owner.id())
				.header("Authorization", owner.bearer()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("OWNER_CANNOT_LEAVE"));
		mvc.perform(delete("/api/teams/{teamId}/members/{userId}", teamId, b.id()).header("Authorization", owner.bearer()))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/teams/{teamId}/members", teamId).header("Authorization", b.bearer()))
				.andExpect(status().isForbidden());
	}

	// ---------- 발송 ----------

	@Test
	void dueNotificationsAreSentAndFutureOnesWait() throws Exception {
		TestUser user = signupAndLogin("send");
		long eventId = createEvent(user, "발송 테스트");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");

		dispatcher.dispatchDue(Instant.parse("2026-12-09T00:00:00Z"), 100);
		assertThat(outbox(user.id(), eventId)).singleElement()
				.satisfies(row -> assertThat(row.get("status")).isEqualTo("PENDING"));

		dispatcher.dispatchDue(Instant.parse("2026-12-10T00:00:00Z"), 100);
		assertThat(outbox(user.id(), eventId)).singleElement()
				.satisfies(row -> assertThat(row.get("status")).isEqualTo("SENT"));

		mvc.perform(get("/api/notifications").header("Authorization", user.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].status").value("SENT"))
				.andExpect(jsonPath("$[0].eventId").value(eventId))
				.andExpect(jsonPath("$[0].readAt").doesNotExist());
	}

	@Test
	void concurrentDispatchersNeverSendTheSameNotificationTwice() throws Exception {
		TestUser owner = signupAndLogin("owner");
		List<TestUser> team = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			team.add(signupAndLogin("m" + i));
		}
		long teamId = createTeam(owner, team.toArray(TestUser[]::new));
		long eventId = idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("동시 발송", START, END)))
				.andExpect(status().isCreated()).andReturn());
		List<Long> mine = jdbc.queryForList("select id from notification_outbox where event_id = ?", Long.class, eventId);
		assertThat(mine).hasSize(8);

		List<Long> sentIds = Collections.synchronizedList(new ArrayList<>());
		Answer<?> realCall = Mockito.mockingDetails(sender).getMockCreationSettings().getDefaultAnswer();
		doAnswer(invocation -> {
			sentIds.add(invocation.<NotificationSender.OutgoingNotification>getArgument(0).id());
			Thread.sleep(50);
			return realCall.answer(invocation);
		}).when(sender).send(any());

		ExecutorService pool = Executors.newFixedThreadPool(3);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			results.add(pool.submit(() -> {
				go.await();
				return dispatcher.dispatchDue(Instant.now().plus(Duration.ofMinutes(1)), 3);
			}));
		}
		go.countDown();
		for (Future<Integer> result : results) {
			result.get();
		}
		// 남은 것이 있으면 마저 처리한다.
		while (dispatcher.dispatchDue(Instant.now().plus(Duration.ofMinutes(1)), 3) > 0) {
		}
		pool.shutdown();

		Map<Long, Long> timesSent = sentIds.stream().filter(mine::contains)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		assertThat(timesSent).hasSize(8).allSatisfy((id, times) -> assertThat(times).isEqualTo(1));
		assertThat(jdbc.queryForObject("select count(*) from notification_outbox where event_id = ? and status = 'SENT'",
				Long.class, eventId)).isEqualTo(8);
	}

	@Test
	void failedSendIsRetriedWithBackoffThenSucceeds() throws Exception {
		TestUser user = signupAndLogin("retry");
		long eventId = createEvent(user, "재시도");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");
		Instant due = Instant.parse("2026-12-10T00:00:00Z");

		Answer<?> realCall = Mockito.mockingDetails(sender).getMockCreationSettings().getDefaultAnswer();
		java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
		doAnswer(invocation -> {
			if (invocation.<NotificationSender.OutgoingNotification>getArgument(0).eventId() == eventId
					&& calls.incrementAndGet() <= 2) {
				throw new IllegalStateException("채널 오류");
			}
			return realCall.answer(invocation);
		}).when(sender).send(any());

		dispatcher.dispatchDue(due, 100);
		assertThat(outbox(user.id(), eventId)).singleElement().satisfies(row -> {
			assertThat(row.get("status")).isEqualTo("PENDING");
			assertThat(row.get("attempt_count")).isEqualTo(1);
		});
		// 다음 시도 시각(1분 뒤) 전에는 다시 보내지 않는다.
		dispatcher.dispatchDue(due.plusSeconds(30), 100);
		assertThat(calls.get()).isEqualTo(1);

		dispatcher.dispatchDue(due.plus(Duration.ofMinutes(1)), 100);
		assertThat(calls.get()).isEqualTo(2);
		dispatcher.dispatchDue(due.plus(Duration.ofMinutes(3)), 100);
		assertThat(outbox(user.id(), eventId)).singleElement().satisfies(row -> {
			assertThat(row.get("status")).isEqualTo("SENT");
			assertThat(row.get("attempt_count")).isEqualTo(3);
		});
	}

	@Test
	void sendFailingTooManyTimesBecomesFailed() throws Exception {
		TestUser user = signupAndLogin("fail");
		long eventId = createEvent(user, "계속 실패");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");
		doThrow(new IllegalStateException("채널 오류")).when(sender).send(any());

		Instant now = Instant.parse("2026-12-10T00:00:00Z");
		for (int i = 0; i < 6; i++) {
			dispatcher.dispatchDue(now, 100);
			now = now.plus(Duration.ofHours(1));
		}
		assertThat(outbox(user.id(), eventId)).singleElement().satisfies(row -> {
			assertThat(row.get("status")).isEqualTo("FAILED");
			assertThat(row.get("attempt_count")).isEqualTo(5);
		});
	}

	@Test
	void logsDoNotContainTitleOrMemo(CapturedOutput output) throws Exception {
		TestUser user = signupAndLogin("log");
		long eventId = createEvent(user, "아주비밀한제목");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");

		dispatcher.dispatchDue(Instant.parse("2026-12-10T00:00:00Z"), 100);

		assertThat(output.getOut()).contains("알림 발송(가짜)").doesNotContain("아주비밀한제목").doesNotContain("비밀 메모");
	}

	// ---------- API ----------

	@Test
	void notificationsCanBeReadOnlyByOwner() throws Exception {
		TestUser user = signupAndLogin("reader");
		TestUser other = signupAndLogin("other");
		long eventId = createEvent(user, "읽음 테스트");
		setReminder(user, eventId, "2026-12-10T09:00:00+09:00");
		long notificationId = ((Number) outbox(user.id(), eventId).get(0).get("id")).longValue();

		mvc.perform(post("/api/notifications/{id}/read", notificationId).header("Authorization", other.bearer()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));
		mvc.perform(post("/api/notifications/{id}/read", notificationId).header("Authorization", user.bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.readAt").isNotEmpty());
		mvc.perform(get("/api/notifications").header("Authorization", other.bearer()))
				.andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(get("/api/notifications").param("status", "PENDING").header("Authorization", user.bearer()))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].kind").value("EVENT_REMINDER"));
		mvc.perform(get("/api/notifications").param("status", "NOPE").header("Authorization", user.bearer()))
				.andExpect(status().isBadRequest());
	}

}
