package com.planit.notification;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 정해진 간격으로 발송 시각이 지난 알림을 처리한다. 한 번에 batchSize개씩, 남은 것이 없을 때까지 반복한다. */
@Component
@ConditionalOnProperty(name = "planit.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

	private final NotificationDispatcher dispatcher;
	private final int batchSize;

	public NotificationScheduler(NotificationDispatcher dispatcher,
			@Value("${planit.notifications.batch-size:50}") int batchSize) {
		this.dispatcher = dispatcher;
		this.batchSize = batchSize;
	}

	/** 입력값: 없음 / 출력값: 없음 / 기능: 대기 알림을 모두 처리할 때까지 묶음 단위로 보낸다. */
	@Scheduled(fixedDelayString = "${planit.notifications.dispatch-interval:30s}")
	public void run() {
		while (dispatcher.dispatchDue(Instant.now(), batchSize) == batchSize) {
			// 한 묶음이 꽉 찼으면 남은 알림이 더 있을 수 있으므로 계속 처리한다.
		}
	}

}
