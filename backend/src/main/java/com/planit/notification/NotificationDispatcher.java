package com.planit.notification;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.planit.event.Event;
import com.planit.event.EventRepository;
import com.planit.team.TeamMemberRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 발송 시각이 지난 PENDING 알림을 보낸다. 행을 FOR UPDATE SKIP LOCKED로 잠그고 같은 트랜잭션에서 상태를 바꾸므로
 * 여러 서버·스레드가 동시에 실행해도 한 알림은 한 번만 처리된다.
 * 실패하면 maxAttempts까지 지수적으로 늘어나는 간격(retryBackoff × 2^(시도-1))으로 다시 시도하고, 그 뒤에는 FAILED가 된다.
 */
@Component
public class NotificationDispatcher {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

	private final NotificationRepository notifications;
	private final NotificationSender sender;
	private final EventRepository events;
	private final TeamMemberRepository members;
	private final int maxAttempts;
	private final Duration retryBackoff;

	public NotificationDispatcher(NotificationRepository notifications, NotificationSender sender,
			EventRepository events, TeamMemberRepository members,
			@Value("${planit.notifications.max-attempts:5}") int maxAttempts,
			@Value("${planit.notifications.retry-backoff:1m}") Duration retryBackoff) {
		this.notifications = notifications;
		this.sender = sender;
		this.events = events;
		this.members = members;
		this.maxAttempts = maxAttempts;
		this.retryBackoff = retryBackoff;
	}

	/**
	 * 입력값: 기준 시각, 한 번에 처리할 최대 개수
	 * 출력값: 이번에 가져온 알림 수
	 * 기능: 대상이 아직 유효한지(일정이 남아 있는지, 팀 일정이면 아직 팀원인지) 다시 확인한 뒤 보내고 결과를 기록한다.
	 */
	@Transactional
	public int dispatchDue(Instant now, int batchSize) {
		List<Notification> due = notifications.lockDue(now, batchSize);
		for (Notification notification : due) {
			if (!stillRelevant(notification)) {
				notification.cancel();
				continue;
			}
			try {
				sender.send(new NotificationSender.OutgoingNotification(notification.getId(), notification.getUserId(),
						notification.getKind(), notification.getEventId()));
				notification.markSent(now);
			}
			catch (RuntimeException e) {
				int attempt = notification.getAttemptCount() + 1;
				Instant next = attempt >= maxAttempts ? null : now.plus(retryBackoff.multipliedBy(1L << (attempt - 1)));
				notification.markAttemptFailed(e.getClass().getSimpleName(), next);
				log.warn("알림 발송 실패: notificationId={}, attempt={}, error={}, willRetry={}", notification.getId(),
						attempt, e.getClass().getSimpleName(), next != null);
			}
		}
		return due.size();
	}

	private boolean stillRelevant(Notification notification) {
		Event event = events.findById(notification.getEventId()).orElse(null);
		if (event == null) {
			return false;
		}
		if (notification.getKind() == Notification.Kind.EVENT_REMINDER && event.getDeletedAt() != null) {
			return false;
		}
		if (event.isTeamEvent()) {
			return members.isMember(event.getTeamId(), notification.getUserId());
		}
		return event.getCreatedBy().equals(notification.getUserId());
	}

}
