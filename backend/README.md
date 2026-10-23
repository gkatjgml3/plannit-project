# 플랜잇 백엔드

Spring Boot 4 · Java 17 · PostgreSQL · Flyway · JWT(액세스 + 리프레시)

## 실행 준비

1. **Java 17 이상** (`java -version`으로 확인)
2. **PostgreSQL 16**에 DB와 계정을 만듭니다.

   ```sql
   create role planit login password 'planit';
   create database planit owner planit;
   ```

## 실행과 테스트

```bash
./gradlew bootRun   # 개발 서버 (http://localhost:8080). Windows에서는 gradlew.bat
./gradlew test      # 테스트 (H2 메모리 DB 사용, PostgreSQL 없어도 됨)
```

- `gradlew bootRun`은 `SPRING_PROFILES_ACTIVE`를 따로 주지 않으면 **local 프로필**로 실행합니다. local 프로필에는 내 컴퓨터용 DB 계정(`planit`/`planit`)과 고정 JWT 키가 들어 있고 API 문서가 열립니다.
- **운영**은 빌드한 jar를 기본 프로필(프로필 없음)로 실행합니다. 기본 프로필에는 비밀값 기본값이 없어서 `DB_URL`·`DB_USERNAME`·`DB_PASSWORD`·`JWT_SECRET`이 없으면 서버가 시작되지 않습니다.
- **dev 프로필**(`SPRING_PROFILES_ACTIVE=dev`)은 공용 개발 서버용입니다. 비밀값은 운영처럼 환경변수로 받고 API 문서만 엽니다.
- 서버가 시작되면 Flyway가 `src/main/resources/db/migration`의 SQL로 테이블을 만들고, JPA는 엔티티와 테이블이 맞는지만 검사합니다(`ddl-auto: validate`). 테이블을 바꿀 때는 이미 적용된 파일을 고치지 말고 `V8__설명.sql`처럼 새 파일을 추가하세요.

> 프로젝트가 OneDrive 폴더 안에 있으면 `build/` 파일이 잠겨 `Unable to delete directory ... build` 오류로 빌드가 실패할 수 있습니다. 코드 문제가 아니므로 `build` 폴더를 지운 뒤 다시 실행하세요.

## API 문서

local·dev 프로필에서 서버를 띄우면 열립니다. 운영(기본) 프로필에서는 닫혀 있습니다(404).

- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI JSON: http://localhost:8080/v3/api-docs

모든 API에 요청·응답 예시와 오류 코드가 적혀 있습니다. Swagger UI 오른쪽 위 **Authorize**에 로그인으로 받은 accessToken을 넣으면 바로 호출해 볼 수 있습니다.

## 환경변수

| 환경변수 | 기본값 | 설명 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | (bootRun은 `local`) | `local`·`dev`·없음(운영) |
| `DB_URL` | 운영: 필수 / local: `jdbc:postgresql://localhost:5432/planit` | DB 주소 |
| `DB_USERNAME` | 운영: 필수 / local: `planit` | DB 계정 |
| `DB_PASSWORD` | 운영: 필수 / local: `planit` | DB 비밀번호 |
| `JWT_SECRET` | 운영·dev: **필수** / local: 고정 개발용 값 | 토큰 서명 키(32바이트 이상). 없으면 서버가 시작되지 않음 |
| `JWT_ACCESS_TOKEN_TTL` | `30m` | 액세스 토큰 유효 시간 |
| `JWT_REFRESH_TOKEN_TTL` | `14d` | 리프레시 토큰 유효 시간 |
| `AI_ANALYZER` | `fake` | 사용할 AI 분석기. 지금은 개발·테스트용 `fake`만 있음 |
| `AI_FAKE_DELAY` | `0s` | 가짜 분석기가 분석마다 기다릴 시간(비동기 확인용, 예: `3s`) |
| `AI_FAKE_FAIL_FIRST` | `0` | 가짜 분석기가 서버 시작 후 처음 N번 분석을 실패시킴(재시도 확인용) |
| `ANALYSIS_WORKER_THREADS` | `2` | 분석 실행기 스레드 수 |
| `ANALYSIS_PROCESSING_TIMEOUT` | `5m` | 이 시간보다 오래 `processing`에 머문 분석은 복구 대상 |
| `IMAGE_STORAGE_TYPE` | `local` | 이미지 저장소: `local`(서버 폴더) 또는 `memory`(테스트용) |
| `IMAGE_STORAGE_DIR` | `data/images` | `local` 저장소 폴더 (git에 올리지 않음) |
| `IMAGE_RETENTION` | `7d` | 확정되지 않은 분석의 원본 이미지 보관 기간 |

그 밖의 세부 값(알림 재시도 횟수·간격, 정리 주기 등)은 `src/main/resources/application.yml`의 `planit.*`에 있습니다.

## 주요 API 흐름

`/health`, `/api/auth/**` 외에는 `Authorization: Bearer <accessToken>`이 필요합니다. 오류는 `{"code": "...", "message": "..."}` 형태입니다. 시간은 ISO 8601로 주고받고, DB에는 `timestamptz`로, 시간대는 `timezone` 필드(기본 `Asia/Seoul`)에 따로 저장합니다.

1. **로그인**: `POST /api/auth/signup` → `POST /api/auth/login` → `{accessToken(30분), refreshToken(14일)}`
2. **토큰 갱신**: 액세스 토큰이 만료되면 `POST /api/auth/refresh {refreshToken}` → 새 토큰 한 쌍. 보낸 리프레시 토큰은 다시 쓸 수 없습니다.
3. **일정**: `POST /api/events`(준비물 `checklist`, 반복 `recurrence` 포함 가능) → `GET /api/events?from=&to=`로 캘린더 조회(반복 회차가 펼쳐져 나옴)
4. **알림**: `PUT /api/events/{id}/settings {reminderAt}` → 그 시각에 알림이 처리되고 `GET /api/notifications`에 나타남
5. **팀**: `POST /api/teams` → `POST /api/teams/{teamId}/members {email}` → `POST /api/teams/{teamId}/events` → 팀원 캘린더에 함께 보이고 다른 팀원에게 변경 알림
6. **이미지 분석**: `POST /api/analyses`(multipart `image`) → **202**와 `pending` → `GET /api/analyses/{id}`로 `needs_review`가 될 때까지 확인 → 후보 수정 → `POST /api/analyses/{id}/confirm {candidateIds}` → 일정 생성, 원본 이미지 삭제

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/health` | 서버·DB 상태 |
| POST | `/api/auth/signup` | 회원가입 `{email, password(8자 이상), name}` |
| POST | `/api/auth/login` | 로그인 → 액세스·리프레시 토큰 |
| POST | `/api/auth/refresh` | 토큰 재발급(리프레시 토큰 교체) |
| POST | `/api/auth/logout` | 리프레시 토큰 폐기 |
| POST | `/api/events` | 개인 일정 생성 (`Idempotency-Key` 헤더 선택) |
| GET | `/api/events?from=2026-11-01&to=2026-11-30` | 내 개인 일정 + 내가 속한 팀 일정, 반복 회차 펼침 (최대 366일) |
| GET | `/api/events/{id}` | 일정 하나 조회 (반복 일정은 원본) |
| PUT | `/api/events/{id}` | 일정 수정 (`version` 필수, 전체 교체) |
| DELETE | `/api/events/{id}` | 일정 삭제 |
| PUT | `/api/events/{id}/settings` | 나에게만 적용되는 설정 `{hidden, reminderAt, personalMemo}` |
| POST | `/api/teams` | 팀 생성 (만든 사람이 팀장) |
| GET | `/api/teams` | 내가 속한 팀 |
| POST | `/api/teams/{teamId}/members` | 팀원 추가 `{email}` (팀장만) |
| GET | `/api/teams/{teamId}/members` | 팀원 목록 |
| DELETE | `/api/teams/{teamId}/members/{userId}` | 본인 탈퇴 또는 팀장이 내보내기 |
| POST | `/api/teams/{teamId}/events` | 팀 일정 생성 |
| POST | `/api/analyses` | 이미지 분석 요청 → 202 (multipart: `image`, 선택 `teamId`, 선택 `timezone`) |
| GET | `/api/analyses/{id}` | 분석 상태와 일정 후보 조회 |
| PUT | `/api/analyses/{id}/candidates/{candidateId}` | 후보 수정 |
| POST | `/api/analyses/{id}/confirm` | 선택한 후보 확정 `{candidateIds: [...]}` |
| POST | `/api/analyses/{id}/retry` | 실패한 분석 다시 시도 → 202 |
| GET | `/api/notifications` | 내 알림 목록 (선택 `status`) |
| POST | `/api/notifications/{id}/read` | 알림 읽음 처리 |

일정 생성 요청 예시:

```json
{
  "title": "수학 학원",
  "startsAt": "2026-11-02T09:00:00+09:00",
  "endsAt": "2026-11-02T10:00:00+09:00",
  "timezone": "Asia/Seoul",
  "location": "본관 302호",
  "checklist": ["교과서", "필통"],
  "recurrence": {"frequency": "WEEKLY", "interval": 1, "until": "2026-12-31", "byDays": ["MO", "WE"]}
}
```

## 규칙

### 인증

- 액세스 토큰은 HS256 JWT(발급자 `planit`, 기본 30분)입니다. 서명·만료·발급자를 검사합니다.
- 리프레시 토큰은 256비트 임의 문자열이며 DB에는 **SHA-256 해시만** 저장합니다. 재발급할 때마다 새 토큰으로 바뀝니다(rotation).
- 이미 사용한 리프레시 토큰이 다시 오면 탈취로 보고 그 사용자의 리프레시 토큰을 **모두** 폐기합니다(`401 REFRESH_TOKEN_REUSED`). 로그아웃한 토큰은 `401 REFRESH_TOKEN_REVOKED`입니다.
- 로그아웃은 리프레시 토큰만 폐기합니다. 이미 발급된 액세스 토큰은 만료될 때까지 쓸 수 있습니다.
- 비밀번호는 BCrypt로 저장합니다. 로그에는 토큰·비밀번호를 남기지 않습니다.

### 일정

- **팀 일정·반복 회차는 복사하지 않습니다**: 원본 한 행만 `events`에 저장하고, 조회할 때 팀 소속과 반복 규칙으로 펼칩니다. 숨김·알림·메모처럼 사람마다 다른 값은 `event_user_settings`에 저장합니다.
- **준비물**: `checklist`(최대 50개, 각 200자). 수정은 전체 교체이므로 빼면 지워집니다.
- **반복**: `frequency`(DAILY·WEEKLY·MONTHLY), `interval`(1~365, 기본 1), `until`(그날 포함) 또는 `count`(1~1000) 중 하나, WEEKLY의 `byDays`(MO~SU, 시작 요일이 들어 있어야 하고 비우면 시작 요일). MONTHLY는 시작일과 같은 날짜에 반복하고 그 날짜가 없는 달은 건너뜁니다. 틀리면 `400 INVALID_RECURRENCE`.
- **회차 펼치기**: 일정의 `timezone` 현지 시각 기준으로 펼칩니다. 매주 월요일 09:00(KST)은 어떤 시간대로 조회해도 KST 09:00이고, 서머타임이 있는 지역도 현지 시각이 유지됩니다. 캘린더 응답에서 반복 회차의 `id`는 원본 일정 id, `occurrenceStartsAt`은 회차 시작 시각입니다.
- **중복 저장 방지**: `Idempotency-Key`(100자 이하)를 보내면 같은 키·같은 내용의 재시도는 처음 만든 일정을 200으로 돌려줍니다. 같은 키를 다른 내용(제목·시각·시간대·설명·장소·준비물·반복·개인/팀 중 하나라도 다름)에 쓰면 `409 IDEMPOTENCY_KEY_REUSED`. 비교는 요청 지문(SHA-256)으로 합니다.
- **권한**: 개인 일정은 만든 사람만 보고 고칩니다. 팀 일정은 팀원 모두 보고, 만든 사람과 팀장만 고칩니다. 볼 수 없는 일정은 404, 팀원이 아닌 사람의 팀 API 요청은 403입니다.
- **동시 수정**: 수정할 때 보낸 `version`이 현재와 다르거나 같은 version으로 동시에 수정되면 `409 EVENT_VERSION_CONFLICT`와 현재 값(`current`)을 돌려줍니다(`events.version` 낙관적 잠금).

### 알림

- `notification_outbox`에 일정 변경과 같은 트랜잭션으로 쌓고, 스케줄러가 발송 시각이 지난 `PENDING` 알림을 처리합니다(기본 30초 간격).
- 종류: `EVENT_REMINDER`(내가 정한 알림 시각), `TEAM_EVENT_CREATED`·`UPDATED`·`DELETED`(작성자를 뺀 팀원에게).
- 알림 시각을 정하면 생성, 바꾸면 갱신, 지우면 취소. 일정 시작 시각이 바뀌면 알림 시각도 같은 만큼 옮기고, 일정을 지우면 일정 알림을 취소합니다.
- 팀을 떠나면 그 팀 일정의 대기 알림이 취소되고, 발송 직전에도 아직 팀원인지 다시 확인합니다.
- 여러 서버가 동시에 돌아도 `SELECT ... FOR UPDATE SKIP LOCKED`로 한 알림은 한 번만 처리됩니다. 실패하면 1분·2분·4분… 간격으로 다시 시도하고 5번 실패하면 `FAILED`입니다.
- 실제 발송 채널은 아직 없습니다. `NotificationSender`의 가짜 구현이 로그만 남기며, 로그에는 id와 종류만 있고 제목·메모는 없습니다.

## AI 일정 분석

AI 제공 업체는 아직 정하지 않았습니다(`[미정]`). 분석은 교체 가능한 `ScheduleAnalyzer` 인터페이스 뒤에 있고, 지금은 실제 AI를 호출하지 않는 **개발·테스트용 가짜 분석기**(`FakeScheduleAnalyzer`)만 있습니다. 가짜 분석기의 기본 결과에는 "개발용 가짜 분석기가 만든 예시 결과"라는 경고가 붙습니다. Gemini 같은 실제 업체는 `ScheduleAnalyzer` 구현을 추가하고 `AI_ANALYZER`로 고르면 됩니다. 업체 전용 응답 형식은 그 구현 안에서만 `AnalyzerResult`로 바꿉니다.

이미지는 `ImageStorage` 인터페이스로 저장합니다(현재 `local`·`memory` 구현). DB에는 이미지 원본이 아니라 저장 키만 들어가고, 키는 응답에 나오지 않습니다. JPEG·PNG·WEBP만 받고(파일 앞부분 시그니처로 검사), 10MB까지 올릴 수 있습니다.

### 비동기 처리

- 업로드하면 분석 요청을 저장하고 바로 **202**와 `pending`을 돌려줍니다. 분석은 별도 실행기(스레드 풀)가 처리합니다.
- 실행기는 `pending → processing`으로 바꾸며 가져가는데, 낙관적 잠금 덕분에 여러 스레드·서버 중 한 곳만 가져갑니다. 분석기는 DB 트랜잭션 밖에서 호출합니다.
- 실행기가 놓친 `pending`은 10초마다 다시 집어 갑니다. `processing`에 5분 넘게 머문 분석(서버가 처리 중 꺼진 경우 등)은 시도 횟수가 남았으면 다시 `pending`으로, 아니면 `failed`(`ANALYSIS_TIMEOUT`)로 바꿉니다. 서버가 시작될 때도 한 번 실행합니다. 복구 전의 처리가 늦게 끝나도 그 결과는 버립니다.

### 분석 상태

| 상태 | 뜻 |
| --- | --- |
| `pending` | 이미지를 저장하고 실행기를 기다리는 중 |
| `processing` | 분석 중 |
| `needs_review` | 후보가 나왔고 사용자가 확인·수정할 차례 (일정이 없으면 후보 0개와 경고) |
| `failed` | 분석 실패. `failureCode`(예: `AI_TIMEOUT`, `AI_PROVIDER_ERROR`, `AI_INVALID_RESPONSE`, `ANALYSIS_TIMEOUT`, `IMAGE_MISSING`)와 메시지가 남고 재시도할 수 있음(최대 5번) |
| `confirmed` | 선택한 후보를 일정으로 저장함. 더 고치거나 확정할 수 없음 |

### 후보 필드

`title`, `startDate`(yyyy-MM-dd), `startTime`·`endTime`(HH:mm), `endDate`, `timezone`, `location`, `materials`(준비물), `description`, `recurring`·`recurrenceNote`(반복 여부와 읽은 문장), `recurrence`(반복 규칙), `uncertainFields`(모호한 필드 이름), `warnings`, `eventId`(확정되면 만든 일정 id).
날짜·시간·반복 규칙은 분석기가 읽은 그대로 보관합니다. 서버는 비어 있는 값을 추측해서 채우지 않습니다.

### 후보 수정

`PUT /api/analyses/{id}/candidates/{candidateId}`로 후보 전체 값을 보냅니다(`timezone` 필수). 값이 바뀐 필드는 `uncertainFields`에서 빠집니다. 값은 그대로 두되 확인했다는 필드는 `resolvedFields: ["location"]`처럼 보내면 빠집니다. `needs_review` 상태에서만 고칠 수 있습니다.

### 확정 규칙

- 선택한 후보만 일정으로 저장합니다. 준비물은 일정의 `checklist`로, 반복 규칙은 일정의 `recurrence`로 저장합니다.
- 다음 중 하나라도 해당하는 후보가 있으면 `400 CANDIDATE_VALIDATION_FAILED`와 후보별 이유(`errors`)를 돌려주고 **아무것도 저장하지 않습니다**.
  - 날짜가 없음 (`CANDIDATE_DATE_MISSING`)
  - 날짜·시간이 모호함 (`CANDIDATE_DATE_UNCERTAIN`), 그 밖의 모호한 필드가 남아 있음 (`CANDIDATE_FIELD_UNCERTAIN`)
  - 제목이 없음 (`CANDIDATE_TITLE_MISSING`)
  - 2월 30일처럼 달력에 없는 날짜 (`INVALID_DATE`), 틀린 시간 형식 (`INVALID_TIME`)
  - 시작이 종료보다 늦거나 같음 (`INVALID_TIME_RANGE`)
  - 시작·종료 시간 중 하나만 있음 (`CANDIDATE_TIME_INCOMPLETE`)
  - 반복 일정인데 규칙이 없음 (`CANDIDATE_RECURRENCE_MISSING`), 규칙이 틀림 (`CANDIDATE_RECURRENCE_INVALID`)
  - 이 분석의 후보가 아님 (`CANDIDATE_NOT_FOUND`)
- 시간이 둘 다 없으면 종일 일정(시작일 00:00 ~ 종료일 다음 날 00:00)으로 저장합니다. 종료일이 없으면 시작일과 같은 날로 봅니다.
- 확정은 하나의 트랜잭션입니다. 저장 도중 오류가 나면 이미 저장한 일정도 모두 되돌립니다.
- 개인 분석은 개인 일정으로, 팀 분석(`teamId`로 만든 분석)은 팀 일정으로 저장합니다. 팀 분석은 만들 때와 확정할 때 모두 팀원인지 확인합니다.

### 이미지 보관

- 원본 이미지는 분석이 **확정되면 바로** 지웁니다. 확정되지 않은 분석은 업로드 후 `IMAGE_RETENTION`(기본 7일)이 지나면 지웁니다(처리 중인 분석은 제외).
- 지운 뒤에도 분석 기록과 후보는 남고, 응답의 `imageDeleted: true`로 표시됩니다. 이미지가 지워진 실패 분석은 재시도할 수 없습니다(`409 IMAGE_DELETED`).
- 정리는 1시간마다 돕니다. 저장소 삭제가 실패하면 기록을 남기지 않으므로 다음 정리 때 다시 시도합니다.

### 권한과 로그

- 분석은 만든 사람만 조회·수정·확정·재시도할 수 있습니다. 같은 팀원이라도 다른 사람에게는 `404 ANALYSIS_NOT_FOUND`입니다.
- 로그에는 분석 id와 오류 코드만 남깁니다. 이미지 내용, 저장 키, AI 원문 응답, API 키는 남기지 않습니다.

## Flyway 마이그레이션 기록

| 버전 | 파일 | 내용 |
| --- | --- | --- |
| V1 | `V1__init.sql` | `users`, `teams`, `team_members`, `events`, `event_user_settings` 생성. `event_user_settings.event_id → events.id`, `event_user_settings.user_id → users.id` 외래 키 포함 |
| V2 | `V2__idempotency_fingerprint_and_image_analysis.sql` | `events.request_fingerprint` 추가. AI 분석용 `image_analyses`, `analysis_candidates` 생성 |
| V3 | `V3__refresh_tokens.sql` | `refresh_tokens`(해시, 만료·사용·폐기 시각) 생성 |
| V4 | `V4__checklist_and_recurrence.sql` | `event_checklist_items` 생성, `events`에 반복 규칙 열과 검사 제약 추가, `analysis_candidates`에 반복 규칙 열 추가 |
| V5 | `V5__notification_outbox.sql` | `notification_outbox` 생성 |
| V6 | `V6__image_retention.sql` | `image_analyses.image_deleted_at` 추가 |
| V7 | `V7__analysis_async.sql` | `image_analyses.processing_started_at` 추가 |

빈 DB와 V2까지 적용된 DB 모두에서 V7까지 적용되는 것을 확인했습니다.

## 남은 작업

- 반복 일정의 "이 회차만 수정·삭제"(예외 회차)
- 반복 일정의 알림은 원본 일정의 알림 시각 하나만 처리합니다. 회차마다 알림을 보내는 기능은 없습니다.
- 실제 AI 업체 연결(`ScheduleAnalyzer` 구현), 실제 푸시·이메일 발송(`NotificationSender` 구현), 클라우드 이미지 저장소(`ImageStorage` 구현)
- 만료·폐기된 리프레시 토큰과 오래된 알림 행 정리

> Windows에서 curl로 한글이 들어간 JSON을 보낼 때는 `-d '...'` 대신 파일에 저장해 `--data-binary @파일.json`으로 보내세요. 명령줄 인자는 UTF-8이 아닌 인코딩으로 바뀌어 400 오류가 납니다.
