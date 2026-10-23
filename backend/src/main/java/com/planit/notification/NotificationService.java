package com.planit.notification;

import java.time.Instant;
import java.util.List;

import com.planit.common.ApiException;
import com.planit.team.TeamMember;
import com.planit.team.TeamMemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 outbox에 행을 만들고·바꾸고·취소한다. 일정 변경과 같은 트랜잭션에서 호출되므로,
 * 일정 변경이 롤백되면 알림도 함께 롤백된다. 실제 발송은 {@link NotificationDispatcher}가 한다.
 */
@Service
public class NotificationService {

	private final NotificationRepository notifications;
	private final TeamMemberRepository members;

	public NotificationService(NotificationRepository notifications, TeamMemberRepository members) {
		this.notifications = notifications;
		this.members = members;
	}

	/**
	 * 입력값: 일정 id, 사용자 id, 새 알림 시각(지우면 null)
	 * 출력값: 없음
	 * 기능: 그 사용자의 대기 중인 일정 알림을 새 시각으로 바꾸거나, 없으면 만들고, null이면 취소한다.
	 */
	@Transactional
	public void reminderChanged(Long eventId, Long userId, Instant reminderAt) {
		var pending = notifications.findFirstByUserIdAndEventIdAndKindAndStatus(userId, eventId,
				Notification.Kind.EVENT_REMINDER, Notification.Status.PENDING);
		if (reminderAt == null) {
			pending.ifPresent(Notification::cancel);
			return;
		}
		if (pending.isPresent()) {
			pending.get().reschedule(reminderAt);
		}
		else {
			notifications.save(new Notification(userId, Notification.Kind.EVENT_REMINDER, eventId, reminderAt));
		}
	}

	/** 입력값: 일정 id / 출력값: 없음 / 기능: 일정이 삭제되면 그 일정의 대기 중인 일정 알림을 모두 취소한다. */
	@Transactional
	public void cancelReminders(Long eventId) {
		notifications.findByEventIdAndKindAndStatus(eventId, Notification.Kind.EVENT_REMINDER,
				Notification.Status.PENDING).forEach(Notification::cancel);
	}

	/**
	 * 입력값: 일정 id, 팀 id, 변경한 사람, 변경 종류
	 * 출력값: 없음
	 * 기능: 변경한 사람을 뺀 현재 팀원에게 바로 보낼 변경 알림을 만든다.
	 */
	@Transactional
	public void teamEventChanged(Long eventId, Long teamId, Long actorId, Notification.Kind kind) {
		Instant now = Instant.now();
		for (TeamMember member : members.findByIdTeamIdOrderByJoinedAt(teamId)) {
			if (!member.getUserId().equals(actorId)) {
				notifications.save(new Notification(member.getUserId(), kind, eventId, now));
			}
		}
	}

	/** 입력값: 팀 id, 떠난 사용자 id / 출력값: 없음 / 기능: 그 팀 일정에 대한 그 사용자의 대기 알림을 취소한다. */
	@Transactional
	public void memberLeft(Long teamId, Long userId) {
		notifications.cancelPendingForTeamMember(teamId, userId, Instant.now());
	}

	/**
	 * 입력값: 사용자 id, 상태 필터(없으면 취소된 것을 뺀 전부)
	 * 출력값: 최근 100개 알림
	 * 기능: 내 알림 목록을 발송 예정 시각이 늦은 순으로 돌려준다.
	 */
	@Transactional(readOnly = true)
	public List<NotificationResponse> list(Long userId, Notification.Status status) {
		List<Notification.Status> statuses = status != null ? List.of(status)
				: List.of(Notification.Status.PENDING, Notification.Status.SENT, Notification.Status.FAILED);
		return notifications.findTop100ByUserIdAndStatusInOrderByScheduledAtDescIdDesc(userId, statuses).stream()
				.map(NotificationResponse::of).toList();
	}

	/** 입력값: 사용자 id, 알림 id / 출력값: 읽음 처리한 알림 / 기능: 다른 사람의 알림이면 404. */
	@Transactional
	public NotificationResponse markRead(Long userId, Long notificationId) {
		Notification notification = notifications.findById(notificationId)
				.filter(n -> n.getUserId().equals(userId))
				.orElseThrow(() -> ApiException.notFound("NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다."));
		notification.markRead(Instant.now());
		return NotificationResponse.of(notification);
	}

	/** 알림 응답. 일정 제목 같은 내용은 넣지 않으며, 필요하면 eventId로 일정을 조회한다(권한 검사 포함). */
	public record NotificationResponse(Long id, Notification.Kind kind, Long eventId, Instant scheduledAt,
			Notification.Status status, int attemptCount, Instant sentAt, Instant readAt, Instant createdAt) {

		static NotificationResponse of(Notification n) {
			return new NotificationResponse(n.getId(), n.getKind(), n.getEventId(), n.getScheduledAt(), n.getStatus(),
					n.getAttemptCount(), n.getSentAt(), n.getReadAt(), n.getCreatedAt());
		}

	}

}
