package com.planit.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** 리프레시 토큰 한 개. 원문 대신 SHA-256 해시만 가진다. */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "used_at")
	private Instant usedAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	protected RefreshToken() {
	}

	RefreshToken(Long userId, String tokenHash, Instant expiresAt) {
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	/** 입력값: 없음 / 출력값: 이미 재발급에 쓴 토큰이면 true / 기능: 재사용 감지에 쓴다. */
	boolean isUsed() {
		return usedAt != null;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

}
