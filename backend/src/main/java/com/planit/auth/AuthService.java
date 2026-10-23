package com.planit.auth;

import java.time.Instant;
import java.util.Locale;

import com.planit.auth.AuthDtos.LoginRequest;
import com.planit.auth.AuthDtos.SignupRequest;
import com.planit.auth.AuthDtos.TokenResponse;
import com.planit.auth.AuthDtos.UserResponse;
import com.planit.common.ApiException;
import com.planit.config.JwtProperties;
import com.planit.user.User;
import com.planit.user.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
			JwtProperties jwtProperties) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

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

	public TokenResponse login(LoginRequest request) {
		User user = users.findByEmail(normalizeEmail(request.email()))
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));

		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("planit")
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.expiration()))
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.build();
		String token = jwtEncoder
				.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
		return new TokenResponse(token, "Bearer", jwtProperties.expiration().toSeconds());
	}

	static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
