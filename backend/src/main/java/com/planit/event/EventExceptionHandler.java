package com.planit.event;

import com.planit.event.EventDtos.EventResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class EventExceptionHandler {

	/** 버전 충돌 응답. current는 요청한 사용자가 지금 볼 수 있는 일정의 최신 값이다. */
	record VersionConflictResponse(String code, String message, EventResponse current) {
	}

	/** 입력값: 버전 충돌 / 출력값: 409 EVENT_VERSION_CONFLICT와 현재 값 / 기능: 덮어쓰기를 막고 최신 값을 알려 준다. */
	@ExceptionHandler(EventVersionConflictException.class)
	ResponseEntity<VersionConflictResponse> handleVersionConflict(EventVersionConflictException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new VersionConflictResponse("EVENT_VERSION_CONFLICT", e.getMessage(), e.getCurrent()));
	}

}
