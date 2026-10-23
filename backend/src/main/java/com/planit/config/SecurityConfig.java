package com.planit.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.planit.auth.AuthService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/health", "/api/auth/**", "/error").permitAll()
						// API 문서. 운영 프로필에서는 springdoc이 꺼져 있어 404가 된다.
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
						.permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 입력값: JWT 설정 / 출력값: HMAC 서명 키
	 * 기능: 키가 없거나 32바이트보다 짧으면 서버 시작을 실패시킨다. 기본 프로필에는 기본값이 없으므로
	 * 운영에서 JWT_SECRET을 빠뜨리면 서버가 뜨지 않는다. local·test 프로필만 고정 키를 쓴다.
	 */
	@Bean
	public SecretKey jwtSecretKey(JwtProperties properties) {
		if (properties.secret() == null || properties.secret().isBlank()) {
			throw new IllegalStateException("JWT_SECRET 환경변수가 없습니다. 32바이트 이상의 임의 값을 설정하세요.");
		}
		byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET은 32바이트 이상이어야 합니다.");
		}
		if (properties.accessTokenTtl() == null || properties.refreshTokenTtl() == null) {
			throw new IllegalStateException("planit.jwt.access-token-ttl과 refresh-token-ttl이 필요합니다.");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	/** 입력값: 서명 키 / 출력값: JWT 검증기 / 기능: 서명(HS256), 만료 시각, 발급자(planit)를 검사한다. */
	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(AuthService.ISSUER));
		return decoder;
	}

}
