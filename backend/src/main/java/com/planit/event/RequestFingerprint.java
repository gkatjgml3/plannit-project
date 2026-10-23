package com.planit.event;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/**
 * 일정 생성 요청의 내용을 SHA-256 값으로 만든다.
 * 같은 사용자가 같은 Idempotency-Key로 보낸 요청이 처음과 같은 내용인지 비교하는 데 쓴다.
 * 시각은 Instant로 바꿔서 비교하므로 "+09:00"과 "Z"처럼 표기만 다른 같은 시각은 같은 요청으로 본다.
 */
final class RequestFingerprint {

	private RequestFingerprint() {
	}

	/**
	 * 입력값: 팀 id(개인 일정이면 null), 제목, 시작·종료 시각, 시간대, 설명, 장소, 준비물 목록, 반복 규칙(없으면 null)
	 * 출력값: 64자리 16진수 SHA-256 문자열
	 * 기능: 각 값 앞에 길이를 붙여 이어 붙인 뒤 해시해서, 값 경계가 섞여 다른 요청이 같은 값이 되는 일을 막는다.
	 */
	static String of(Long teamId, String title, Instant startsAt, Instant endsAt, String timezone,
			String description, String location, List<String> checklist, RecurrenceRule recurrence) {
		StringBuilder canonical = new StringBuilder();
		append(canonical, teamId == null ? null : teamId.toString());
		append(canonical, title);
		append(canonical, startsAt.toString());
		append(canonical, endsAt.toString());
		append(canonical, timezone);
		append(canonical, description);
		append(canonical, location);
		append(canonical, String.valueOf(checklist.size()));
		checklist.forEach(item -> append(canonical, item));
		append(canonical, recurrence == null ? null : recurrence.canonical());
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256")
					.digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}

	/**
	 * 입력값: 저장된 일정과 준비물 / 출력값: 지문
	 * 기능: V2 이전에 저장되어 지문이 없는 일정의 지문을 현재 값으로 계산한다.
	 */
	static String of(Event event, List<String> checklist) {
		return of(event.getTeamId(), event.getTitle(), event.getStartsAt(), event.getEndsAt(), event.getTimezone(),
				event.getDescription(), event.getLocation(), checklist, event.getRecurrence());
	}

	private static void append(StringBuilder canonical, String value) {
		if (value == null) {
			canonical.append("-1:");
			return;
		}
		canonical.append(value.length()).append(':').append(value);
	}

}
