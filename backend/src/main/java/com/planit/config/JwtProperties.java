package com.planit.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * secret: JWT 서명 키(32바이트 이상). 기본 프로필에는 기본값이 없어서 JWT_SECRET 환경변수가 없으면 서버가 뜨지 않는다.
 * accessTokenTtl: 액세스 토큰 유효 시간, refreshTokenTtl: 리프레시 토큰 유효 시간.
 */
@ConfigurationProperties("planit.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
}
