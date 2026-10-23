package com.planit.common;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 요청한 사용자의 id를 꺼내는 유일한 곳.
 * 로그인 방식이 바뀌면 이 클래스만 고치면 된다.
 */
public final class CurrentUser {

	private CurrentUser() {
	}

	public static Long id(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}

}
