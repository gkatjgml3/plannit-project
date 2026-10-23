package com.planit.event;

import com.planit.event.EventDtos.EventResponse;

/** 수정 요청의 version이 현재 일정의 version과 다를 때. 클라이언트가 다시 맞출 수 있도록 현재 값을 함께 돌려준다. */
public class EventVersionConflictException extends RuntimeException {

	private final EventResponse current;

	public EventVersionConflictException(EventResponse current) {
		super("다른 곳에서 먼저 수정되었습니다. 현재 값을 확인한 뒤 다시 시도하세요.");
		this.current = current;
	}

	public EventResponse getCurrent() {
		return current;
	}

}
