package com.planit.auth;

import com.planit.auth.AuthDtos.LoginRequest;
import com.planit.auth.AuthDtos.RefreshRequest;
import com.planit.auth.AuthDtos.SignupRequest;
import com.planit.auth.AuthDtos.TokenResponse;
import com.planit.auth.AuthDtos.UserResponse;
import com.planit.common.ApiExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입, 로그인, 토큰 재발급, 로그아웃. 토큰 없이 호출한다.")
@SecurityRequirements
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** 입력값: 이메일·비밀번호·이름 / 출력값: 201과 만든 사용자 / 기능: 회원가입. */
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "회원가입", description = "비밀번호는 8자 이상이며 BCrypt 해시로 저장된다.\n\n"
			+ "오류: 400 VALIDATION_FAILED, 409 EMAIL_TAKEN")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.SIGNUP_REQUEST)))
	@ApiResponse(responseCode = "201", content = @Content(examples = @ExampleObject(value = ApiExamples.USER)))
	@ApiResponse(responseCode = "409", description = "EMAIL_TAKEN", content = @Content(examples = @ExampleObject(value = ApiExamples.ERROR)))
	public UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	/** 입력값: 이메일·비밀번호 / 출력값: 토큰 한 쌍 / 기능: 로그인. */
	@PostMapping("/login")
	@Operation(summary = "로그인", description = "액세스 토큰(기본 30분)과 리프레시 토큰(기본 14일)을 발급한다.\n\n"
			+ "오류: 400 VALIDATION_FAILED, 401 INVALID_CREDENTIALS")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.LOGIN_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.TOKENS)))
	@ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS", content = @Content(examples = @ExampleObject(value = ApiExamples.ERROR)))
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	/** 입력값: 리프레시 토큰 / 출력값: 새 토큰 한 쌍 / 기능: 액세스 토큰을 다시 발급하고 리프레시 토큰을 교체한다. */
	@PostMapping("/refresh")
	@Operation(summary = "토큰 재발급", description = "보낸 리프레시 토큰은 사용 처리되고 새 리프레시 토큰이 발급된다(rotation). "
			+ "이미 사용한 토큰을 다시 보내면 탈취로 보고 그 사용자의 리프레시 토큰을 모두 폐기한다.\n\n"
			+ "오류: 401 INVALID_REFRESH_TOKEN, 401 REFRESH_TOKEN_EXPIRED, 401 REFRESH_TOKEN_REVOKED, 401 REFRESH_TOKEN_REUSED")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.REFRESH_REQUEST)))
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = ApiExamples.TOKENS)))
	@ApiResponse(responseCode = "401", description = "토큰이 없거나 만료·폐기·재사용됨", content = @Content(examples = @ExampleObject(value = """
			{"code": "REFRESH_TOKEN_REUSED", "message": "이미 사용한 리프레시 토큰입니다. 보안을 위해 다시 로그인하세요."}""")))
	public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request.refreshToken());
	}

	/** 입력값: 리프레시 토큰 / 출력값: 204 / 기능: 그 리프레시 토큰을 폐기한다. 액세스 토큰은 만료될 때까지 유효하다. */
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기한다. 모르는 토큰이어도 204. "
			+ "이미 발급된 액세스 토큰은 만료(기본 30분)될 때까지 쓸 수 있다.")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ApiExamples.REFRESH_REQUEST)))
	@ApiResponse(responseCode = "204", description = "폐기함")
	@ApiResponse(responseCode = "400", description = "VALIDATION_FAILED(refreshToken 없음)", content = @Content(examples = @ExampleObject(value = """
			{"code": "VALIDATION_FAILED", "message": "refreshToken: 공백일 수 없습니다"}""")))
	public void logout(@Valid @RequestBody RefreshRequest request) {
		authService.logout(request.refreshToken());
	}

}
