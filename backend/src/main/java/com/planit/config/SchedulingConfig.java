package com.planit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 주기 작업(알림 발송, 이미지 정리, 분석 복구)을 켠다. 테스트에서는 planit.scheduling.enabled=false로 끄고 직접 호출한다. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "planit.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
