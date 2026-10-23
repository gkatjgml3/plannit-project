package com.planit.notification;

/**
 * 알림을 실제로 전달하는 경계. 푸시·이메일 같은 실제 채널은 아직 [미정]이라 로그만 남기는 가짜 구현만 있다.
 * 실패하면 예외를 던진다. 디스패처가 재시도한다.
 */
public interface NotificationSender {

	/** 입력값: 보낼 알림 / 출력값: 없음 / 기능: 알림을 전달한다. 일정 제목·메모 같은 내용은 넘기지 않는다. */
	void send(OutgoingNotification notification);

	/** 전달할 알림. 내용은 받는 쪽이 eventId로 다시 조회한다. */
	record OutgoingNotification(Long id, Long userId, Notification.Kind kind, Long eventId) {
	}

}
