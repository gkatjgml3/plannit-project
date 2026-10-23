package com.planit.auth;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** 변경 쿼리는 각각 자기 트랜잭션에서 바로 커밋된다. 재사용 감지 후 401을 던져도 폐기 기록은 남아야 하기 때문이다. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	/**
	 * 입력값: 토큰 id, 현재 시각 / 출력값: 바뀐 행 수(0 또는 1)
	 * 기능: 아직 쓰지 않은 토큰만 사용 처리한다. 같은 토큰으로 동시에 재발급하면 한 요청만 1을 받는다.
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update RefreshToken t set t.usedAt = :now where t.id = :id and t.usedAt is null and t.revokedAt is null")
	int markUsed(@Param("id") Long id, @Param("now") Instant now);

	/** 입력값: 사용자 id, 현재 시각 / 출력값: 폐기한 토큰 수 / 기능: 그 사용자의 남은 리프레시 토큰을 모두 폐기한다. */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
	int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);

	/** 입력값: 토큰 id, 현재 시각 / 출력값: 폐기한 토큰 수 / 기능: 토큰 하나를 폐기한다(로그아웃). */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :now where t.id = :id and t.revokedAt is null")
	int revoke(@Param("id") Long id, @Param("now") Instant now);

}
