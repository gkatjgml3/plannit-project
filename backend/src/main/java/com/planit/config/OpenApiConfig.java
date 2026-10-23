package com.planit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** API 문서의 공통 정보. 문서 화면은 local·dev 프로필에서만 열린다(application*.yml의 springdoc 설정). */
@Configuration
public class OpenApiConfig {

	static final String BEARER = "bearerAuth";

	/**
	 * 입력값: 없음 / 출력값: OpenAPI 정의
	 * 기능: 제목·설명과 Bearer JWT 인증 방식을 등록한다. /health와 /api/auth/**를 뺀 모든 API는 토큰이 필요하다.
	 */
	@Bean
	OpenAPI planitOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("플랜잇 (PlanIt) API")
						.version("v1")
						.description("""
								사진 한 장으로 일정을 등록하는 AI 캘린더의 백엔드 API.

								- 시간은 ISO 8601(예: 2026-11-02T09:00:00+09:00), 시간대는 별도 `timezone` 필드(기본 Asia/Seoul).
								- 오류 응답은 `{"code": "...", "message": "..."}` 형태다. 일부 오류는 추가 필드(current, errors)를 가진다.
								- 인증: `POST /api/auth/login`으로 받은 accessToken을 `Authorization: Bearer <토큰>`으로 보낸다.
								  토큰이 없거나 만료·위조되면 401(본문 없음).
								"""))
				.components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER));
	}

}
