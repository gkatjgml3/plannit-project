package com.planit.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record SignupRequest(
			@NotBlank @Email @Size(max = 255) String email,
			@NotBlank @Size(min = 8, max = 100) String password,
			@NotBlank @Size(max = 100) String name) {
	}

	public record LoginRequest(@NotBlank String email, @NotBlank String password) {
	}

	/** 재발급·로그아웃 요청. 리프레시 토큰은 URL이 아니라 본문으로 받는다. */
	public record RefreshRequest(@NotBlank @Size(max = 200) String refreshToken) {
	}

	public record UserResponse(Long id, String email, String name) {
	}

	/** expiresIn·refreshExpiresIn은 초 단위. */
	public record TokenResponse(String accessToken, String tokenType, long expiresIn, String refreshToken,
			long refreshExpiresIn) {
	}

}
