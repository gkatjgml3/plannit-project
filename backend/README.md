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

## 실행과 테스트

```bash
./gradlew bootRun   # 서버 실행 (http://localhost:8080). Windows에서는 gradlew.bat
./gradlew test      # 테스트 (H2 메모리 DB 사용, PostgreSQL 없어도 됨)
```

서버가 시작되면 Flyway가 `src/main/resources/db/migration`의 SQL로 테이블을 만듭니다.
테이블을 바꿀 때는 기존 파일을 고치지 말고 `V2__설명.sql`처럼 새 파일을 추가하세요.

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

- **중복 저장 방지**: 일정 생성 때 `Idempotency-Key` 헤더(100자 이하)를 보내면, 같은 키로 다시 보내도 새로 만들지 않고 처음 만든 일정을 돌려줍니다(처음 201, 다시 보내면 200).
- **팀 일정은 복사하지 않습니다**: `events`에 한 행만 저장하고, 조회할 때 팀 소속 여부로 보여줍니다. 숨김·알림·메모처럼 사람마다 다른 값은 `event_user_settings`에 저장합니다.
- **권한**: 개인 일정은 만든 사람만 보고 고칠 수 있습니다. 팀 일정은 팀원 모두 볼 수 있고, 만든 사람과 팀장만 고칠 수 있습니다. 볼 수 없는 일정은 404, 팀원이 아닌 사람의 팀 API 요청은 403입니다.
- **동시 수정**: 수정할 때 보낸 `version`이 최신이 아니면 409를 돌려줍니다.
- **시간**: `starts_at`·`ends_at`은 `timestamptz`로 저장하고 `timezone`은 따로 저장합니다. 응답의 시각은 일정의 `timezone` 기준입니다.

> Windows에서 curl로 한글이 들어간 JSON을 보낼 때는 `-d '...'` 대신 파일에 저장해 `--data-binary @파일.json`으로 보내세요. 명령줄 인자는 UTF-8이 아닌 인코딩으로 바뀌어 400 오류가 납니다.
