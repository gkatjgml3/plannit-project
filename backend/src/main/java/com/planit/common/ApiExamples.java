package com.planit.common;

/** API 문서에 쓰는 요청·응답 예시 JSON. 실제 동작에는 쓰이지 않는다. */
public final class ApiExamples {

	private ApiExamples() {
	}

	public static final String ERROR = """
			{"code": "VALIDATION_FAILED", "message": "title: 공백일 수 없습니다"}""";

	// ---------- 인증 ----------
	public static final String SIGNUP_REQUEST = """
			{"email": "student@example.com", "password": "password123", "name": "김학생"}""";
	public static final String USER = """
			{"id": 1, "email": "student@example.com", "name": "김학생"}""";
	public static final String LOGIN_REQUEST = """
			{"email": "student@example.com", "password": "password123"}""";
	public static final String TOKENS = """
			{"accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 1800,
			 "refreshToken": "q3k9...43자 임의 문자열", "refreshExpiresIn": 1209600}""";
	public static final String REFRESH_REQUEST = """
			{"refreshToken": "q3k9...43자 임의 문자열"}""";

	// ---------- 일정 ----------
	public static final String EVENT_REQUEST = """
			{"title": "수학 학원", "startsAt": "2026-11-02T09:00:00+09:00", "endsAt": "2026-11-02T10:00:00+09:00",
			 "timezone": "Asia/Seoul", "description": "3단원", "location": "본관 302호",
			 "checklist": ["교과서", "필통"],
			 "recurrence": {"frequency": "WEEKLY", "interval": 1, "until": "2026-12-31", "byDays": ["MO", "WE"]}}""";
	public static final String EVENT_UPDATE_REQUEST = """
			{"title": "수학 학원(시간 변경)", "startsAt": "2026-11-02T10:00:00+09:00",
			 "endsAt": "2026-11-02T11:00:00+09:00", "timezone": "Asia/Seoul", "version": 0,
			 "checklist": ["교과서"], "recurrence": {"frequency": "WEEKLY", "count": 10}}""";
	public static final String EVENT = """
			{"id": 10, "teamId": null, "createdBy": 1, "title": "수학 학원",
			 "startsAt": "2026-11-02T09:00:00+09:00", "endsAt": "2026-11-02T10:00:00+09:00", "timezone": "Asia/Seoul",
			 "description": "3단원", "location": "본관 302호", "version": 0,
			 "createdAt": "2026-10-24T01:00:00Z", "updatedAt": "2026-10-24T01:00:00Z",
			 "hidden": false, "reminderAt": null, "personalMemo": null,
			 "checklist": [{"id": 1, "content": "교과서", "sortOrder": 0}, {"id": 2, "content": "필통", "sortOrder": 1}],
			 "recurrence": {"frequency": "WEEKLY", "interval": 1, "until": "2026-12-31", "count": null, "byDays": ["MO", "WE"]},
			 "occurrenceStartsAt": null}""";
	public static final String CALENDAR = """
			[{"id": 10, "teamId": null, "createdBy": 1, "title": "수학 학원",
			  "startsAt": "2026-11-04T09:00:00+09:00", "endsAt": "2026-11-04T10:00:00+09:00", "timezone": "Asia/Seoul",
			  "version": 0, "hidden": false, "checklist": [{"id": 1, "content": "교과서", "sortOrder": 0}],
			  "recurrence": {"frequency": "WEEKLY", "interval": 1, "until": "2026-12-31", "count": null, "byDays": ["MO", "WE"]},
			  "occurrenceStartsAt": "2026-11-04T09:00:00+09:00"},
			 {"id": 12, "teamId": 3, "createdBy": 2, "title": "동아리 회의",
			  "startsAt": "2026-11-05T17:00:00+09:00", "endsAt": "2026-11-05T18:00:00+09:00", "timezone": "Asia/Seoul",
			  "version": 1, "hidden": false, "checklist": [], "recurrence": null, "occurrenceStartsAt": null}]""";
	public static final String VERSION_CONFLICT = """
			{"code": "EVENT_VERSION_CONFLICT", "message": "다른 곳에서 먼저 수정되었습니다. 현재 값을 확인한 뒤 다시 시도하세요.",
			 "current": {"id": 10, "title": "다른 사람이 바꾼 제목", "version": 1, "...": "일정 전체 값"}}""";
	public static final String SETTINGS_REQUEST = """
			{"hidden": false, "reminderAt": "2026-11-02T08:30:00+09:00", "personalMemo": "숙제 챙기기"}""";

	// ---------- 팀 ----------
	public static final String TEAM_REQUEST = """
			{"name": "1학년 3반"}""";
	public static final String TEAM = """
			{"id": 3, "name": "1학년 3반", "myRole": "OWNER", "createdAt": "2026-10-24T01:00:00Z"}""";
	public static final String MEMBER_REQUEST = """
			{"email": "friend@example.com"}""";
	public static final String MEMBER = """
			{"userId": 2, "email": "friend@example.com", "name": "이친구", "role": "MEMBER", "joinedAt": "2026-10-24T01:00:00Z"}""";

	// ---------- 분석 ----------
	public static final String ANALYSIS_PENDING = """
			{"id": 7, "status": "pending", "teamId": null, "timezone": "Asia/Seoul", "warnings": [],
			 "failureCode": null, "failureMessage": null, "attemptCount": 0, "createdAt": "2026-10-24T01:00:00Z",
			 "updatedAt": "2026-10-24T01:00:00Z", "confirmedAt": null, "imageDeleted": false, "imageDeletedAt": null,
			 "candidates": []}""";
	public static final String ANALYSIS_REVIEW = """
			{"id": 7, "status": "needs_review", "teamId": null, "timezone": "Asia/Seoul", "warnings": ["표 일부가 잘렸습니다."],
			 "failureCode": null, "failureMessage": null, "attemptCount": 1, "imageDeleted": false,
			 "candidates": [
			  {"id": 21, "position": 0, "title": "과학 수행평가", "startDate": "2026-12-01", "startTime": "09:00",
			   "endDate": null, "endTime": "10:00", "timezone": "Asia/Seoul", "location": "과학실",
			   "materials": ["실험복"], "description": null, "recurring": false, "recurrenceNote": null,
			   "uncertainFields": ["location"], "warnings": ["장소 글씨가 흐립니다."], "eventId": null, "recurrence": null},
			  {"id": 22, "position": 1, "title": "영어 단어 시험", "startDate": null, "startTime": null,
			   "endDate": null, "endTime": null, "timezone": "Asia/Seoul", "location": null, "materials": [],
			   "description": null, "recurring": true, "recurrenceNote": "매주 금요일",
			   "uncertainFields": ["startDate"], "warnings": ["날짜를 찾지 못했습니다."], "eventId": null,
			   "recurrence": {"frequency": "WEEKLY", "interval": 1, "until": null, "count": null, "byDays": ["FR"]}}]}""";
	public static final String CANDIDATE_UPDATE_REQUEST = """
			{"title": "영어 단어 시험", "startDate": "2026-12-04", "startTime": "08:30", "endTime": "08:50",
			 "timezone": "Asia/Seoul", "materials": ["단어장"], "recurring": true,
			 "recurrence": {"frequency": "WEEKLY", "count": 4, "byDays": ["FR"]}, "resolvedFields": []}""";
	public static final String CANDIDATE = """
			{"id": 22, "position": 1, "title": "영어 단어 시험", "startDate": "2026-12-04", "startTime": "08:30",
			 "endDate": null, "endTime": "08:50", "timezone": "Asia/Seoul", "location": null, "materials": ["단어장"],
			 "description": null, "recurring": true, "recurrenceNote": "매주 금요일", "uncertainFields": [],
			 "warnings": ["날짜를 찾지 못했습니다."], "eventId": null,
			 "recurrence": {"frequency": "WEEKLY", "interval": null, "until": null, "count": 4, "byDays": ["FR"]}}""";
	public static final String CONFIRM_REQUEST = """
			{"candidateIds": [21, 22]}""";
	public static final String CONFIRM_RESPONSE = """
			{"analysis": {"id": 7, "status": "confirmed", "imageDeleted": true, "...": "분석 전체 값"},
			 "createdEventIds": [40, 41]}""";
	public static final String CANDIDATE_ERRORS = """
			{"code": "CANDIDATE_VALIDATION_FAILED", "message": "확정할 수 없는 후보가 있어 아무 일정도 저장하지 않았습니다.",
			 "errors": [{"candidateId": 22, "code": "CANDIDATE_DATE_MISSING", "message": "날짜가 없습니다. 날짜를 입력한 뒤 확정하세요."}]}""";

	// ---------- 알림 ----------
	public static final String NOTIFICATIONS = """
			[{"id": 5, "kind": "EVENT_REMINDER", "eventId": 10, "scheduledAt": "2026-11-01T23:30:00Z", "status": "SENT",
			  "attemptCount": 1, "sentAt": "2026-11-01T23:30:05Z", "readAt": null, "createdAt": "2026-10-24T01:00:00Z"},
			 {"id": 6, "kind": "TEAM_EVENT_UPDATED", "eventId": 12, "scheduledAt": "2026-10-24T02:00:00Z", "status": "PENDING",
			  "attemptCount": 0, "sentAt": null, "readAt": null, "createdAt": "2026-10-24T02:00:00Z"}]""";
	public static final String NOTIFICATION = """
			{"id": 5, "kind": "EVENT_REMINDER", "eventId": 10, "scheduledAt": "2026-11-01T23:30:00Z", "status": "SENT",
			 "attemptCount": 1, "sentAt": "2026-11-01T23:30:05Z", "readAt": "2026-11-02T00:10:00Z",
			 "createdAt": "2026-10-24T01:00:00Z"}""";

}
