package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jayway.jsonpath.JsonPath;
import com.planit.event.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/** 팀장 O와 팀원 A(팀 일정 작성자)가 같은 팀 일정을 같은 version으로 고치는 상황. */
class VersionConflictTest extends ApiTestSupport {

	private static final String START = "2026-11-20T15:00:00+09:00";
	private static final String END = "2026-11-20T16:00:00+09:00";

	@MockitoSpyBean
	private EventRepository events;

	private TestUser owner;
	private TestUser member;
	private long eventId;

	@BeforeEach
	void setUp() throws Exception {
		owner = signupAndLogin("o");
		member = signupAndLogin("a");
		long teamId = idOf(mvc.perform(post("/api/teams").header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"조별 과제\"}")).andReturn());
		mvc.perform(post("/api/teams/{teamId}/members", teamId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(member.email())))
				.andExpect(status().isCreated());
		eventId = idOf(mvc.perform(post("/api/teams/{teamId}/events", teamId).header("Authorization", member.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(eventJson("발표 준비", START, END)))
				.andExpect(status().isCreated()).andReturn());
	}

	private String update(String title, long version) {
		return """
				{"title":"%s","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","version":%d}
				""".formatted(title, START, END, version);
	}

	private MockHttpServletResponse send(TestUser user, String body) throws Exception {
		return mvc.perform(put("/api/events/{id}", eventId).header("Authorization", user.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
	}

	@Test
	void secondUpdateWithSameVersionGetsConflictAndCurrentValue() throws Exception {
		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", member.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update("작성자 수정", 0)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version").value(1));

		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update("팀장 수정", 0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EVENT_VERSION_CONFLICT"))
				.andExpect(jsonPath("$.current.id").value(eventId))
				.andExpect(jsonPath("$.current.version").value(1))
				.andExpect(jsonPath("$.current.title").value("작성자 수정"));

		assertThat(jdbc.queryForObject("select title from events where id = ?", String.class, eventId))
				.isEqualTo("작성자 수정");
	}

	@Test
	void concurrentUpdatesWithSameVersionLetOnlyOneSucceed() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<MockHttpServletResponse>> futures = new ArrayList<>();
		for (TestUser user : new TestUser[] { member, owner }) {
			Callable<MockHttpServletResponse> call = () -> {
				ready.countDown();
				go.await();
				return send(user, update("동시 수정 " + user.id(), 0));
			};
			futures.add(pool.submit(call));
		}
		ready.await();
		go.countDown();
		List<MockHttpServletResponse> responses = new ArrayList<>();
		for (Future<MockHttpServletResponse> future : futures) {
			responses.add(future.get());
		}
		pool.shutdown();

		assertThat(responses).extracting(MockHttpServletResponse::getStatus).containsExactlyInAnyOrder(200, 409);
		MockHttpServletResponse conflict = responses.stream().filter(r -> r.getStatus() == 409).findFirst().orElseThrow();
		assertThat((String) JsonPath.read(conflict.getContentAsString(), "$.code")).isEqualTo("EVENT_VERSION_CONFLICT");
		assertThat(((Number) JsonPath.read(conflict.getContentAsString(), "$.current.version")).longValue()).isEqualTo(1);
		assertThat(jdbc.queryForObject("select version from events where id = ?", Long.class, eventId)).isEqualTo(1);
	}

	@Test
	void updateLosingRaceAfterReadStillReturnsConflictWithCurrentValue() throws Exception {
		// 수정 요청이 일정을 읽은 직후, 다른 트랜잭션이 먼저 같은 일정을 고쳐 커밋하는 상황을 만든다.
		Answer<?> realCall = Mockito.mockingDetails(events).getMockCreationSettings().getDefaultAnswer();
		AtomicBoolean interfere = new AtomicBoolean(true);
		doAnswer(invocation -> {
			Object result = realCall.answer(invocation);
			if (interfere.getAndSet(false)) {
				Thread other = new Thread(() -> jdbc.update(
						"update events set version = version + 1, title = ? where id = ?", "먼저 커밋된 수정", eventId));
				other.start();
				other.join();
			}
			return result;
		}).when(events).findByIdAndDeletedAtIsNull(anyLong());

		mvc.perform(put("/api/events/{id}", eventId).header("Authorization", owner.bearer())
				.contentType(MediaType.APPLICATION_JSON).content(update("늦은 수정", 0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EVENT_VERSION_CONFLICT"))
				.andExpect(jsonPath("$.current.version").value(1))
				.andExpect(jsonPath("$.current.title").value("먼저 커밋된 수정"));

		assertThat(jdbc.queryForObject("select title from events where id = ?", String.class, eventId))
				.isEqualTo("먼저 커밋된 수정");
	}

}
