package com.planit.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("planit.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
