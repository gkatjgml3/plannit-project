package com.planit.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/**
	 * 입력값: 기준 시각, 최대 개수 / 출력값: 잠근 알림 목록
	 * 기능: 다음 시도 시각이 지난 PENDING 알림을 행 잠금과 함께 가져온다. 다른 서버가 이미 잠근 행은 건너뛰므로
	 * 여러 서버가 동시에 돌아도 같은 알림을 두 번 처리하지 않는다. 호출하는 쪽 트랜잭션이 끝날 때 잠금이 풀린다.
	 */
	@Query(value = """
			select * from notification_outbox
			where status = 'PENDING' and next_attempt_at <= :now
			order by next_attempt_at, id
			limit :limit
			for update skip locked
			""", nativeQuery = true)
	List<Notification> lockDue(@Param("now") Instant now, @Param("limit") int limit);

	Optional<Notification> findFirstByUserIdAndEventIdAndKindAndStatus(Long userId, Long eventId,
			Notification.Kind kind, Notification.Status status);

	List<Notification> findByEventIdAndKindAndStatus(Long eventId, Notification.Kind kind, Notification.Status status);

	List<Notification> findTop100ByUserIdAndStatusInOrderByScheduledAtDescIdDesc(Long userId,
			List<Notification.Status> statuses);

	/**
	 * 입력값: 팀 id, 사용자 id / 출력값: 취소한 알림 수
	 * 기능: 팀을 떠난 사용자에게 남아 있는 그 팀 일정의 대기 알림을 모두 취소한다.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update Notification n set n.status = com.planit.notification.Notification.Status.CANCELED,
			       n.updatedAt = :now
			where n.userId = :userId and n.status = com.planit.notification.Notification.Status.PENDING
			  and n.eventId in (select e.id from Event e where e.teamId = :teamId)
			""")
	int cancelPendingForTeamMember(@Param("teamId") Long teamId, @Param("userId") Long userId,
			@Param("now") Instant now);

}
