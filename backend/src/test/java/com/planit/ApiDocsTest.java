package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

class ApiDocsTest {

	/** 운영(기본) 프로필처럼 문서가 꺼진 상태. 테스트 프로필은 기본 설정의 springdoc.enabled=false를 그대로 쓴다. */
	@Nested
	class WhenDisabled extends ApiTestSupport {

		@Test
		void docsAreClosed() throws Exception {
			mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
			mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
		}

	}

	/** dev 프로필(local도 같은 설정)에서는 문서가 열린다. */
	@Nested
	@ActiveProfiles("dev")
	class WhenDevProfile extends ApiTestSupport {

		@Test
		void docsAreOpenWithoutLogin() throws Exception {
			mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
			mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
		}

		@Test
		void everyApiHasSummaryAndResponseExample() throws Exception {
			String json = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();

			Map<String, Map<String, Object>> paths = JsonPath.read(json, "$.paths");
			assertThat(paths.keySet()).contains("/health", "/api/auth/signup", "/api/auth/login", "/api/auth/refresh",
					"/api/auth/logout", "/api/events", "/api/events/{id}", "/api/events/{id}/settings", "/api/teams",
					"/api/teams/{teamId}/members", "/api/teams/{teamId}/members/{userId}", "/api/teams/{teamId}/events",
					"/api/analyses", "/api/analyses/{id}", "/api/analyses/{id}/candidates/{candidateId}",
					"/api/analyses/{id}/confirm", "/api/analyses/{id}/retry", "/api/notifications",
					"/api/notifications/{id}/read");

			int operations = 0;
			for (var path : paths.entrySet()) {
				for (var method : path.getValue().entrySet()) {
					operations++;
					String where = method.getKey().toUpperCase() + " " + path.getKey();
					@SuppressWarnings("unchecked")
					Map<String, Object> op = (Map<String, Object>) method.getValue();
					assertThat(op.get("summary")).as(where + " summary").isNotNull();
					String opJson = com.jayway.jsonpath.JsonPath.parse(op).jsonString();
					// 이름 없는 예시 하나는 example, 여러 개는 examples로 들어간다.
					List<Object> examples = JsonPath.read(opJson, "$.responses..example");
					List<Object> namedExamples = JsonPath.read(opJson, "$.responses..examples");
					assertThat(examples.size() + namedExamples.size()).as(where + " 응답 예시").isPositive();
				}
			}
			// 상태 1 + 인증 4 + 일정 6 + 팀 6 + 분석 5 + 알림 2
			assertThat(operations).isEqualTo(24);

			assertThat((String) JsonPath.read(json, "$.components.securitySchemes.bearerAuth.scheme")).isEqualTo("bearer");
			// 인증 API는 토큰 없이 호출한다.
			assertThat((List<?>) JsonPath.read(json, "$.paths['/api/auth/login'].post.security")).isEmpty();
		}

	}

}
