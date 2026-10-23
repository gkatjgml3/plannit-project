package com.planit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** 테스트마다 새 이메일로 가입해서 테스트끼리 데이터가 섞이지 않게 한다. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestSupport {

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected JdbcTemplate jdbc;

	protected record TestUser(long id, String email, String token) {

		public String bearer() {
			return "Bearer " + token;
		}

	}

	protected TestUser signupAndLogin(String name) throws Exception {
		String email = name + "-" + UUID.randomUUID() + "@test.planit";
		MvcResult signup = mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"password123","name":"%s"}
						""".formatted(email, name)))
				.andExpect(status().isCreated())
				.andReturn();
		long id = ((Number) JsonPath.read(signup.getResponse().getContentAsString(), "$.id")).longValue();

		MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s","password":"password123"}
						""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn();
		return new TestUser(id, email, JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken"));
	}

	protected static String eventJson(String title, String startsAt, String endsAt) {
		return """
				{"title":"%s","startsAt":"%s","endsAt":"%s","timezone":"Asia/Seoul","location":"3층 과학실"}
				""".formatted(title, startsAt, endsAt);
	}

	protected static long idOf(MvcResult result) throws Exception {
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}

}
