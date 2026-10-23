package com.planit.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 가짜 발송기. 실제로 보내지 않고 로그만 남긴다. planit.notifications.sender=log(기본값)일 때 쓴다.
 * 로그에는 id와 종류만 남기고 일정 제목·메모 같은 개인 내용은 남기지 않는다.
 */
@Component
@ConditionalOnProperty(name = "planit.notifications.sender", havingValue = "log", matchIfMissing = true)
public class LoggingNotificationSender implements NotificationSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

	@Override
	public void send(OutgoingNotification notification) {
		log.info("알림 발송(가짜): notificationId={}, userId={}, kind={}, eventId={}", notification.id(),
				notification.userId(), notification.kind(), notification.eventId());
	}

}
