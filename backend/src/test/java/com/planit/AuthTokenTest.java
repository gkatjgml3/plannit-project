package com.planit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.spec.SecretKeySpec;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.planit.config.JwtProperties;
import com.planit.config.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

class AuthTokenTest extends ApiTestSupport {

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtProperties jwtProperties;

	private record Tokens(String access, String refresh) {
	}

	private Tokens login(TestUser user) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"password123\"}".formatted(user.email())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expiresIn").value(1800))
				.andExpect(jsonPath("$.refreshExpiresIn").value(14 * 24 * 3600))
				.andReturn();
		return tokensOf(result);
	}

	private static Tokens tokensOf(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString();
		return new Tokens(JsonPath.read(body, "$.accessToken"), JsonPath.read(body, "$.refreshToken"));
	}

	private ResultActions refresh(String refreshToken) throws Exception {
		return mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)));
	}

	private ResultActions callApi(String accessToken) throws Exception {
		return mvc.perform(get("/api/teams").header("Authorization", "Bearer " + accessToken));
	}

	private String sign(JwtEncoder encoder, Instant issuedAt, Instant expiresAt, String issuer, long userId) {
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(issuedAt).expiresAt(expiresAt)
				.subject(String.valueOf(userId)).build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	@Test
	void accessTokenLastsThirtyMinutes() {
		assertThat(jwtProperties.accessTokenTtl()).isEqualTo(Duration.ofMinutes(30));
		assertThat(jwtProperties.refreshTokenTtl()).isEqualTo(Duration.ofDays(14));
	}

	@Test
	void expiredAccessTokenIsRejected() throws Exception {
		TestUser user = signupAndLogin("expired");
		Instant past = Instant.now().minus(Duration.ofHours(2));
		String expired = sign(jwtEncoder, past, past.plus(Duration.ofMinutes(30)), "planit", user.id());

		callApi(expired).andExpect(status().isUnauthorized());
		callApi(sign(jwtEncoder, Instant.now(), Instant.now().plusSeconds(600), "planit", user.id()))
				.andExpect(status().isOk());
	}

	@Test
	void tokenWithWrongSignatureOrIssuerIsRejected() throws Exception {
		TestUser user = signupAndLogin("forged");
		byte[] otherKey = "another-secret-key-that-attacker-knows-0123".getBytes(StandardCharsets.UTF_8);
		JwtEncoder attacker = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(otherKey, "HmacSHA256")));

		callApi(sign(attacker, Instant.now(), Instant.now().plusSeconds(600), "planit", user.id()))
				.andExpect(status().isUnauthorized());
		callApi(sign(jwtEncoder, Instant.now(), Instant.now().plusSeconds(600), "someone-else", user.id()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshIssuesNewPairAndRotatesToken() throws Exception {
		Tokens first = login(signupAndLogin("refresh"));

		Tokens second = tokensOf(refresh(first.refresh())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn());

		assertThat(second.refresh()).isNotEqualTo(first.refresh());
		callApi(second.access()).andExpect(status().isOk());
		refresh(second.refresh()).andExpect(status().isOk());
	}

	@Test
	void reusedRefreshTokenRevokesAllTokensOfUser() throws Exception {
		TestUser user = signupAndLogin("reuse");
		Tokens first = login(user);
		Tokens otherDevice = login(user);
		Tokens rotated = tokensOf(refresh(first.refresh()).andExpect(status().isOk()).andReturn());

		// 이미 쓴 토큰을 다시 보내면 거부하고, 그 사용자의 다른 리프레시 토큰도 모두 폐기한다.
		refresh(first.refresh())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));
		refresh(rotated.refresh())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REVOKED"));
		refresh(otherDevice.refresh()).andExpect(status().isUnauthorized());
		assertThat(jdbc.queryForObject("select count(*) from refresh_tokens where user_id = ? and revoked_at is null",
				Long.class, user.id())).isZero();
	}

	@Test
	void refreshFailsAfterLogout() throws Exception {
		Tokens tokens = login(signupAndLogin("logout"));

		mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"%s\"}".formatted(tokens.refresh())))
				.andExpect(status().isNoContent());

		refresh(tokens.refresh())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REVOKED"));
	}

	@Test
	void logoutWithUnknownTokenStillSucceeds() throws Exception {
		mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"unknown-token\"}"))
				.andExpect(status().isNoContent());
	}

	@Test
	void unknownOrExpiredRefreshTokenIsRejected() throws Exception {
		TestUser user = signupAndLogin("refexp");
		refresh("not-a-real-token")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

		Tokens tokens = login(user);
		jdbc.update("update refresh_tokens set expires_at = ? where user_id = ?",
				java.sql.Timestamp.from(Instant.now().minusSeconds(60)), user.id());
		refresh(tokens.refresh())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));
	}

	@Test
	void secretsAreStoredOnlyAsHashes() throws Exception {
		TestUser user = signupAndLogin("hash");
		Tokens tokens = login(user);

		String passwordHash = jdbc.queryForObject("select password_hash from users where id = ?", String.class,
				user.id());
		assertThat(passwordHash).startsWith("$2").isNotEqualTo("password123");
		assertThat(jdbc.queryForObject("select count(*) from refresh_tokens where token_hash = ?", Long.class,
				tokens.refresh())).isZero();
		assertThat(jdbc.queryForObject("select count(*) from refresh_tokens where user_id = ?", Long.class,
				user.id())).isGreaterThanOrEqualTo(1);
	}

	@Test
	void missingOrShortSecretFailsStartup() {
		SecurityConfig config = new SecurityConfig();
		Duration ttl = Duration.ofMinutes(30);
		assertThatThrownBy(() -> config.jwtSecretKey(new JwtProperties("", ttl, ttl)))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
		assertThatThrownBy(() -> config.jwtSecretKey(new JwtProperties(null, ttl, ttl)))
				.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> config.jwtSecretKey(new JwtProperties("short", ttl, ttl)))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("32");
	}

}
