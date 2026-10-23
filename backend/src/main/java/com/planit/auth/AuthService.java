package com.planit.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import com.planit.auth.AuthDtos.LoginRequest;
import com.planit.auth.AuthDtos.SignupRequest;
import com.planit.auth.AuthDtos.TokenResponse;
import com.planit.auth.AuthDtos.UserResponse;
import com.planit.common.ApiException;
import com.planit.config.JwtProperties;
import com.planit.user.User;
import com.planit.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * 회원가입·로그인·토큰 재발급·로그아웃.
 * 액세스 토큰은 짧게(기본 30분) 쓰는 JWT이고, 리프레시 토큰은 임의 문자열을 해시로만 DB에 저장한다.
 * 로그에는 토큰과 비밀번호를 남기지 않는다.
 */
@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	public static final String ISSUER = "planit";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository users;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
		this.users = users;
		this.refreshTokens = refreshTokens;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

	/** 입력값: 이메일·비밀번호·이름 / 출력값: 만든 사용자 / 기능: 비밀번호를 BCrypt 해시로 저장해 가입시킨다. */
	public UserResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (users.existsByEmail(email)) {
			throw ApiException.conflict("EMAIL_TAKEN", "이미 가입된 이메일입니다.");
		}
		try {
			User user = users.saveAndFlush(
					new User(email, passwordEncoder.encode(request.password()), request.name().trim()));
			return new UserResponse(user.getId(), user.getEmail(), user.getName());
		}
		catch (DataIntegrityViolationException e) {
			// 같은 이메일로 동시에 가입한 경우
			throw ApiException.conflict("EMAIL_TAKEN", "이미 가입된 이메일입니다.");
		}
	}

	/** 입력값: 이메일·비밀번호 / 출력값: 액세스·리프레시 토큰 / 기능: 비밀번호를 확인하고 새 토큰 한 쌍을 발급한다. */
	public TokenResponse login(LoginRequest request) {
		User user = users.findByEmail(normalizeEmail(request.email()))
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
		return issueTokens(user.getId());
	}

	/**
	 * 입력값: 리프레시 토큰 원문
	 * 출력값: 새 액세스·리프레시 토큰
	 * 기능: 토큰을 한 번 쓰면 사용 처리하고 새 토큰으로 바꾼다(rotation).
	 * 이미 쓴 토큰이 다시 오면 탈취로 보고 그 사용자의 리프레시 토큰을 모두 폐기한다.
	 * 로그아웃 등으로 폐기된(쓰지는 않은) 토큰은 401만 돌려준다.
	 * 폐기는 각각 바로 커밋되므로 이 메서드는 하나의 트랜잭션으로 묶지 않는다.
	 */
	public TokenResponse refresh(String rawToken) {
		Instant now = Instant.now();
		RefreshToken token = refreshTokens.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> ApiException.unauthorized("INVALID_REFRESH_TOKEN", "리프레시 토큰이 올바르지 않습니다."));
		if (token.isUsed()) {
			revokeAllBecauseOfReuse(token.getUserId(), now);
		}
		if (token.getRevokedAt() != null) {
			throw ApiException.unauthorized("REFRESH_TOKEN_REVOKED", "로그아웃되었거나 폐기된 리프레시 토큰입니다. 다시 로그인하세요.");
		}
		if (!token.getExpiresAt().isAfter(now)) {
			throw ApiException.unauthorized("REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다. 다시 로그인하세요.");
		}
		if (refreshTokens.markUsed(token.getId(), now) == 0) {
			// 같은 토큰으로 동시에 재발급을 시도해 다른 요청이 먼저 사용한 경우
			revokeAllBecauseOfReuse(token.getUserId(), now);
		}
		return issueTokens(token.getUserId());
	}

	/** 입력값: 리프레시 토큰 원문 / 출력값: 없음 / 기능: 그 토큰을 폐기한다. 모르는 토큰이어도 오류 없이 끝낸다. */
	public void logout(String rawToken) {
		refreshTokens.findByTokenHash(hash(rawToken))
				.ifPresent(token -> refreshTokens.revoke(token.getId(), Instant.now()));
	}

	private void revokeAllBecauseOfReuse(Long userId, Instant now) {
		int revoked = refreshTokens.revokeAllForUser(userId, now);
		log.warn("재사용된 리프레시 토큰 감지: userId={}, revokedTokens={}", userId, revoked);
		throw ApiException.unauthorized("REFRESH_TOKEN_REUSED", "이미 사용한 리프레시 토큰입니다. 보안을 위해 다시 로그인하세요.");
	}

	private TokenResponse issueTokens(Long userId) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.accessTokenTtl()))
				.subject(String.valueOf(userId))
				.build();
		String accessToken = jwtEncoder
				.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();

		byte[] random = new byte[32];
		RANDOM.nextBytes(random);
		String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
		refreshTokens.save(new RefreshToken(userId, hash(refreshToken), now.plus(jwtProperties.refreshTokenTtl())));

		return new TokenResponse(accessToken, "Bearer", jwtProperties.accessTokenTtl().toSeconds(), refreshToken,
				jwtProperties.refreshTokenTtl().toSeconds());
	}

	/**
	 * 리프레시 토큰은 256비트 임의 값이라 무차별 대입이 불가능하므로 느린 해시 대신 SHA-256으로 충분하다.
	 * 원문은 DB에 남지 않는다.
	 */
	static String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}

	static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
