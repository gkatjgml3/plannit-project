package com.planit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthAndHealthTest extends ApiTestSupport {

	@Test
	void healthReportsDatabaseUp() throws Exception {
		mvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.db").value("UP"));
	}

	@Test
	void flywayCreatesAllTables() {
		for (String table : new String[] { "users", "teams", "team_members", "events", "event_user_settings" }) {
			jdbc.queryForObject("select count(*) from " + table, Long.class);
		}
	}

	@Test
	void signupThenLoginReturnsToken() throws Exception {
		TestUser user = signupAndLogin("auth");
		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30")
				.header("Authorization", user.bearer()))
				.andExpect(status().isOk());
	}

	@Test
	void duplicateEmailIsRejected() throws Exception {
		TestUser user = signupAndLogin("dup");
		mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"password123","name":"dup"}
						""".formatted(user.email().toUpperCase())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
	}

	@Test
	void wrongPasswordIsRejected() throws Exception {
		TestUser user = signupAndLogin("wrongpw");
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"not-the-password"}
						""".formatted(user.email())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void requestWithoutTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/events").param("from", "2026-11-01").param("to", "2026-11-30"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void requestWithForgedTokenIsUnauthorized() throws Exception {
		mvc.perform(get("/api/teams").header("Authorization", "Bearer not.a.real-token"))
				.andExpect(status().isUnauthorized());
	}

}
