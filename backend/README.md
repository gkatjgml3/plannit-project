# 플랜잇 백엔드

Spring Boot 4 · Java 17 · PostgreSQL · Flyway · JWT 로그인

## 실행 준비

1. **Java 17 이상** (`java -version`으로 확인)
2. **PostgreSQL 16**에 DB와 계정을 만듭니다.

   ```sql
   create role planit login password 'planit';
   create database planit owner planit;
   ```

3. 필요하면 환경변수로 설정을 바꿉니다. 없으면 아래 기본값을 씁니다.

   | 환경변수 | 기본값 | 설명 |
   | --- | --- | --- |
   | `DB_URL` | `jdbc:postgresql://localhost:5432/planit` | DB 주소 |
   | `DB_USERNAME` | `planit` | DB 계정 |
   | `DB_PASSWORD` | `planit` | DB 비밀번호 |
   | `JWT_SECRET` | 로컬 개발용 값 | 토큰 서명 키 (32자 이상). **배포할 때는 꼭 바꾸세요.** |
   | `AI_ANALYZER` | `fake` | 사용할 AI 분석기. 지금은 개발·테스트용 `fake`만 있음 |
   | `IMAGE_STORAGE_TYPE` | `local` | 이미지 저장소: `local`(서버 폴더) 또는 `memory`(테스트용) |
   | `IMAGE_STORAGE_DIR` | `data/images` | `local` 저장소 폴더 (git에 올리지 않음) |

## 실행과 테스트

```bash
./gradlew bootRun   # 서버 실행 (http://localhost:8080). Windows에서는 gradlew.bat
./gradlew test      # 테스트 (H2 메모리 DB 사용, PostgreSQL 없어도 됨)
```

서버가 시작되면 Flyway가 `src/main/resources/db/migration`의 SQL로 테이블을 만듭니다.
테이블을 바꿀 때는 이미 적용된 파일을 고치지 말고 `V3__설명.sql`처럼 새 파일을 추가하세요.

> 프로젝트가 OneDrive 폴더 안에 있으면 `build/` 파일이 잠겨 `Unable to delete directory ... build` 오류로 빌드가 실패할 수 있습니다. 코드 문제가 아니므로 `build` 폴더를 지운 뒤 다시 실행하세요.

## Flyway 마이그레이션 기록

| 버전 | 파일 | 내용 |
| --- | --- | --- |
| V1 | `V1__init.sql` | `users`, `teams`, `team_members`, `events`, `event_user_settings` 생성. `event_user_settings.event_id → events.id`, `event_user_settings.user_id → users.id` 외래 키 포함 |
| V2 | `V2__idempotency_fingerprint_and_image_analysis.sql` | `events.request_fingerprint` 추가 (Idempotency-Key 재사용 판별용 SHA-256). AI 분석용 `image_analyses`, `analysis_candidates` 생성 |

## API

`/health`, `/api/auth/**` 외에는 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
오류는 `{"code": "...", "message": "..."}` 형태로 돌려줍니다.

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/health` | 서버·DB 상태 |
| POST | `/api/auth/signup` | 회원가입 `{email, password(8자 이상), name}` |
| POST | `/api/auth/login` | 로그인 → `{accessToken}` (24시간 유효) |
| POST | `/api/events` | 개인 일정 생성 |
| GET | `/api/events?from=2026-11-01&to=2026-11-30` | 내 개인 일정 + 내가 속한 팀 일정 (`timezone` 생략 시 `Asia/Seoul`) |
| GET | `/api/events/{id}` | 일정 하나 조회 |
| PUT | `/api/events/{id}` | 일정 수정 (`version` 필수) |
| DELETE | `/api/events/{id}` | 일정 삭제 |
| PUT | `/api/events/{id}/settings` | 나에게만 적용되는 설정 `{hidden, reminderAt, personalMemo}` |
| POST | `/api/teams` | 팀 생성 (만든 사람이 팀장) |
| GET | `/api/teams` | 내가 속한 팀 |
| POST | `/api/teams/{teamId}/members` | 팀원 추가 `{email}` (팀장만) |
| GET | `/api/teams/{teamId}/members` | 팀원 목록 |
| POST | `/api/teams/{teamId}/events` | 팀 일정 생성 |
| POST | `/api/analyses` | 이미지 분석 생성 (multipart: `image`, 선택 `teamId`, 선택 `timezone`) |
| GET | `/api/analyses/{id}` | 분석 상태와 일정 후보 조회 |
| PUT | `/api/analyses/{id}/candidates/{candidateId}` | 후보 수정 |
| POST | `/api/analyses/{id}/confirm` | 선택한 후보 확정 `{candidateIds: [...]}` → 일정으로 저장 |
| POST | `/api/analyses/{id}/retry` | 실패한 분석 다시 시도 |

일정 생성 요청 예시:

```json
{
  "title": "수학 수행평가",
  "startsAt": "2026-11-02T09:00:00+09:00",
  "endsAt": "2026-11-02T10:00:00+09:00",
  "timezone": "Asia/Seoul",
  "description": "교과서 3단원",
  "location": "3층 과학실"
}
```

## 규칙

- **중복 저장 방지**: 일정 생성 때 `Idempotency-Key` 헤더(100자 이하)를 보내면, 같은 키·같은 내용으로 다시 보내도 새로 만들지 않고 처음 만든 일정을 돌려줍니다(처음 201, 다시 보내면 200). 같은 키를 **다른 내용**(제목·시각·시간대·설명·장소·개인/팀 구분 중 하나라도 다름)에 다시 쓰면 `409 IDEMPOTENCY_KEY_REUSED`입니다. 내용 비교는 요청 지문(SHA-256)으로 하며, `+09:00`과 `Z`처럼 표기만 다른 같은 시각은 같은 내용으로 봅니다. 키는 사용자마다 따로 관리됩니다.
- **팀 일정은 복사하지 않습니다**: `events`에 한 행만 저장하고, 조회할 때 팀 소속 여부로 보여줍니다. 숨김·알림·메모처럼 사람마다 다른 값은 `event_user_settings`에 저장합니다.
- **권한**: 개인 일정은 만든 사람만 보고 고칠 수 있습니다. 팀 일정은 팀원 모두 볼 수 있고, 만든 사람과 팀장만 고칠 수 있습니다. 볼 수 없는 일정은 404, 팀원이 아닌 사람의 팀 API 요청은 403입니다.
- **동시 수정**: 수정할 때 보낸 `version`이 최신이 아니면 409를 돌려줍니다.
- **시간**: `starts_at`·`ends_at`은 `timestamptz`로 저장하고 `timezone`은 따로 저장합니다. 응답의 시각은 일정의 `timezone` 기준입니다.

## AI 일정 분석

AI 제공 업체는 아직 정하지 않았습니다(`[미정]`). 분석은 교체 가능한 `ScheduleAnalyzer` 인터페이스 뒤에 있고, 지금은 실제 AI를 호출하지 않는 **개발·테스트용 가짜 분석기**(`FakeScheduleAnalyzer`)만 있습니다. 가짜 분석기의 기본 결과에는 "개발용 가짜 분석기가 만든 예시 결과"라는 경고가 붙습니다. Gemini 같은 실제 업체는 `ScheduleAnalyzer` 구현을 추가하고 `AI_ANALYZER`로 고르면 됩니다. 업체 전용 응답 형식은 그 구현 안에서만 `AnalyzerResult`로 바꿉니다.

이미지는 `ImageStorage` 인터페이스로 저장합니다(현재 `local`·`memory` 구현). DB에는 이미지 원본이 아니라 저장 키만 들어가고, 키는 응답에 나오지 않습니다. JPEG·PNG·WEBP만 받고(파일 앞부분 시그니처로 검사), 10MB까지 올릴 수 있습니다.

### 분석 상태

| 상태 | 뜻 |
| --- | --- |
| `pending` | 이미지를 저장하고 분석을 기다리는 중 |
| `processing` | 분석 중 |
| `needs_review` | 후보가 나왔고 사용자가 확인·수정할 차례 (일정이 없으면 후보 0개와 경고) |
| `failed` | 분석기 오류. `failureCode`(예: `AI_TIMEOUT`, `AI_INVALID_RESPONSE`)와 메시지가 남고 재시도할 수 있음 (최대 5번) |
| `confirmed` | 선택한 후보를 일정으로 저장함. 더 고치거나 확정할 수 없음 |

### 후보 필드

`title`, `startDate`(yyyy-MM-dd), `startTime`·`endTime`(HH:mm), `endDate`, `timezone`, `location`, `materials`(준비물 목록), `description`, `recurring`·`recurrenceNote`(반복 여부와 내용), `uncertainFields`(모호한 필드 이름), `warnings`, `eventId`(확정되면 만든 일정 id).
날짜·시간은 분석기가 읽은 그대로 보관합니다. 서버는 비어 있는 값을 추측해서 채우지 않습니다.

### 후보 수정

`PUT /api/analyses/{id}/candidates/{candidateId}`로 후보 전체 값을 보냅니다(`timezone` 필수). 값이 바뀐 필드는 `uncertainFields`에서 빠집니다. 값은 그대로 두되 확인했다는 필드는 `resolvedFields: ["location"]`처럼 보내면 빠집니다. `needs_review` 상태에서만 고칠 수 있습니다.

### 확정 규칙

- 선택한 후보만 일정으로 저장합니다. 고르지 않은 후보는 저장하지 않습니다.
- 다음 중 하나라도 해당하는 후보가 있으면 `400 CANDIDATE_VALIDATION_FAILED`와 후보별 이유(`errors`)를 돌려주고 **아무것도 저장하지 않습니다**.
  - 날짜가 없음 (`CANDIDATE_DATE_MISSING`)
  - 날짜·시간이 모호함 (`CANDIDATE_DATE_UNCERTAIN`), 그 밖의 모호한 필드가 남아 있음 (`CANDIDATE_FIELD_UNCERTAIN`)
  - 제목이 없음 (`CANDIDATE_TITLE_MISSING`)
  - 2월 30일처럼 달력에 없는 날짜 (`INVALID_DATE`), 틀린 시간 형식 (`INVALID_TIME`)
  - 시작이 종료보다 늦거나 같음 (`INVALID_TIME_RANGE`)
  - 시작·종료 시간 중 하나만 있음 (`CANDIDATE_TIME_INCOMPLETE`)
  - 이 분석의 후보가 아님 (`CANDIDATE_NOT_FOUND`)
- 시간이 둘 다 없으면 종일 일정(시작일 00:00 ~ 종료일 다음 날 00:00)으로 저장합니다. 종료일이 없으면 시작일과 같은 날로 봅니다.
- 확정은 하나의 트랜잭션입니다. 저장 도중 오류가 나면 이미 저장한 일정도 모두 되돌립니다.
- 개인 분석은 개인 일정으로, 팀 분석(`teamId`로 만든 분석)은 팀 일정으로 저장합니다. 팀 분석은 만들 때와 확정할 때 모두 팀원인지 확인합니다. 저장된 일정에는 기존 일정 권한 정책이 그대로 적용됩니다.

### 권한과 로그

- 분석은 만든 사람만 조회·수정·확정·재시도할 수 있습니다. 같은 팀원이라도 다른 사람에게는 `404 ANALYSIS_NOT_FOUND`입니다.
- 로그에는 분석 id와 오류 코드만 남깁니다. 이미지 내용, 저장 키, AI 원문 응답, API 키는 남기지 않습니다.

### 임시 구현

- 분석은 업로드 요청 안에서 바로 실행됩니다(동기). 실제 AI를 붙이면 비동기 실행으로 바꿔야 합니다.
- `events`에 준비물·반복 열이 없어서, 확정할 때 준비물과 반복 내용을 일정 `description` 뒤에 덧붙입니다. 반복 일정은 한 번만 저장됩니다.
- 이미지 보관 기간과 삭제 정책이 없습니다.

> Windows에서 curl로 한글이 들어간 JSON을 보낼 때는 `-d '...'` 대신 파일에 저장해 `--data-binary @파일.json`으로 보내세요. 명령줄 인자는 UTF-8이 아닌 인코딩으로 바뀌어 400 오류가 납니다.
