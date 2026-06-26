# Feature: 모임 참석 체크

## 1. Goal

로그인한 사용자가 테니스 모임을 만들고 링크를 공유하면, 비로그인 사용자도 이름과 성별을 입력해 참석 여부를 남길 수 있게 한다.
참석자가 확정되면 모임장은 참석자 목록으로 한 모임당 하나의 Competition을 생성해 기존 경기표/클럽 세션 운영 흐름으로 이어간다.

## 2. User Flow

```text
로그인 사용자 -> 모임 만들기 -> 공유 링크 확인
링크 방문자 -> 이름과 성별 입력 -> 참석/불참/미정 선택 -> 응답 저장
모임장 -> 참석자 목록 확인 -> 경기표 생성
시스템 -> Competition 생성 -> Meeting.competitionId 연결 -> 경기표 보기 제공
```

## 3. Spec

- 사용자 화면 용어는 `모임`을 사용하고, 코드/API/DB 도메인은 `Meeting`을 사용한다.
- 모임 생성은 로그인 사용자만 가능하다. 생성자의 user id를 `Meeting.ownerUserId`로 저장한다.
- 모임 owner는 모임 정보를 조회, 수정, 삭제할 수 있다.
- 모임 owner는 참석 응답의 이름, 성별, 참석 상태를 수정하거나 참석 응답을 삭제할 수 있다.
- 공개 참석 화면에서도 사용자는 기존 참석 응답을 선택해 이름, 성별, 참석 상태를 모두 수정할 수 있다.
- 참석 체크는 로그인 없이 가능하며, 이름과 성별은 필수이다.
- 참석 상태는 `ATTENDING`, `NOT_ATTENDING`, `MAYBE`를 지원한다.
- 한 모임에서는 같은 이름의 활성 참석 응답을 중복 생성하지 않는다.
- 한 모임은 최대 하나의 Competition만 연결한다.
- 모임 시간은 시작 시간과 종료 시간을 입력한다. 종료 시간은 시작 시간 이후여야 한다.
- 모임은 총 참석 정원 방식 또는 성별 참석 정원 방식 중 하나만 사용할 수 있다.
- 총 참석 정원을 설정하면 남자/여자 참석 정원은 설정하지 않는다.
- 남자/여자 참석 정원을 설정하면 총 참석 정원은 설정하지 않는다.
- `ATTENDING`으로 저장하거나 변경할 때 선택된 정원 방식의 정원이 이미 찼으면 선택할 수 없다.
- 참석 응답 생성/수정/삭제는 Meeting row 비관적 쓰기 락을 잡고 처리해 같은 모임의 정원 검증과 중복 이름 검사를 직렬화한다.
- 모임은 Competition 생성을 위해 코트 수와 경기 수를 저장한다.
- 모임 삭제는 soft delete로 처리한다.
- 모임 owner는 참석 체크를 마감하거나 다시 열 수 있다. 마감된 모임은 새 참석 응답과 참석 상태 변경을 받지 않는다.
- Meeting 없이 기존 Competition 생성은 계속 가능해야 한다.
- Meeting은 Competition 생성 전에도 독립적으로 조회/관리 가능해야 한다.
- Competition 생성 시 기본 owner는 Meeting owner로 복사한다.
- 모임 owner는 Meeting에 연결된 Competition을 삭제할 수 있다. 이때 Competition은 soft delete하고 `Meeting.competitionId`는 null로 되돌린다.
- Meeting에 연결된 Competition을 삭제하면 같은 Meeting에서 다시 Competition을 생성할 수 있다.
- Competition 생성 후에는 참석 응답 생성/수정을 막는다. 연결된 Competition을 삭제해 `Meeting.competitionId`가 null이 되면 참석 응답을 다시 수정할 수 있다.
- 참석 응답과 CompetitionEntry는 분리한다. 참석 응답은 원본 RSVP이고, CompetitionEntry는 실제 경기/매칭 참가자 스냅샷이다.
- 경기표 생성 후 `Meeting.competitionId`를 저장한다. MeetingAttendance와 CompetitionEntry는 직접 매핑하지 않는다.

## 4. Design Review

### Responsibility

- Controller / API:
- `MeetingController`가 `/api/meetings` 생성, 공개 조회, owner 관리 조회, 수정, 삭제, 상태 변경, 참석 응답, 모임 기반 경기표 생성을 담당한다.
- Service / Application:
  - `MeetingCommandService`는 생성/참석/경기표 생성 명령을 처리한다.
  - `MeetingQueryService`는 공개 링크 상세와 소유자 관리 상세 조회를 처리한다.
- Domain:
  - `Meeting`은 공유 링크, 소유자, 모임 정보, Competition 연결을 가진다.
  - `MeetingAttendance`는 이름/성별 기반 참석 응답만 가진다.
- Repository / Persistence:
  - `MeetingRepository`, `MeetingAttendanceRepository`를 추가한다.
  - `tb_meeting.COMPETITION_ID`는 nullable unique FK로 둔다.
  - 참석 응답 변경 시 `MeetingRepository`의 `PESSIMISTIC_WRITE` 조회로 같은 Meeting의 동시 변경을 직렬화한다.
- Security / External:
  - `POST /api/meetings`와 경기표 생성은 로그인 필요.
  - 공개 모임 조회와 참석 응답은 비로그인 허용.
- Frontend / UI:
  - 내비게이션 또는 내 경기 흐름에서 `모임 만들기` 진입점을 제공한다.
  - `/meetings/:publicId`에서 공개 참석 체크 화면을 제공한다.
  - `/meetings/:publicId/manage`는 owner 전용 관리 화면이다. owner는 이 화면에서 모임 정보 수정, 참석자 관리, 참석 마감/재오픈, 모임 삭제, 경기표 생성/삭제를 수행한다.

### Frontend Routes

```text
/meetings
- 내 모임 목록
- 로그인 필요
- 내가 만든 모임의 상태, 시간, 참석자 수, 경기표 생성 여부 표시

/meetings/new
- 모임 만들기 화면
- 로그인 필요
- 화면 라우트에서만 `/new`를 사용하고, API는 `POST /api/meetings`를 사용

/meetings/:publicId
- 공개 모임 상세와 참석 체크 화면
- 비로그인 접근 가능
- 모임 정보, 참석/미정/불참 명단, 남/녀/총 참석 카운트, 정원 상태 표시
- 참석 응답 팝업에서 이름, 성별, 참석 여부 입력 또는 기존 응답 수정

/meetings/:publicId/manage
- 모임장 관리 화면
- 로그인 owner만 접근
- 기본 정보 수정, 참석자 수정/삭제, 참석 마감/재오픈, 공유 링크 복사, 경기표 생성/삭제, 모임 삭제
```

### Data Model

```text
tb_meeting
- MEETING_ID PK
- PUBLIC_ID unique
- OWNER_USER_ID not null
- COMPETITION_ID nullable unique
- TITLE not null
- START_AT not null
- END_AT not null
- NOTE nullable
- MAX_PARTICIPANTS nullable
- MAX_MALE_PARTICIPANTS nullable
- MAX_FEMALE_PARTICIPANTS nullable
- COURT_COUNT not null
- TOTAL_GAMES not null
- STATUS not null: OPEN, CLOSED, CANCELLED
- DEL_DT nullable
- CREATE_DT / UPDATE_DT

tb_meeting_attendance
- MEETING_ATTENDANCE_ID PK
- MEETING_ID not null FK
- PARTICIPANT_NAME not null
- ATTENDANCE_STATUS not null: ATTENDING, NOT_ATTENDING, MAYBE
- GENDER not null: MALE, FEMALE
- DEL_DT nullable
- CREATE_DT / UPDATE_DT
```

### Review Questions

- Meeting과 Competition의 owner는 각각의 리소스 소유자이므로 중복이 아니라 독립 권한 기준이다.
- Meeting은 Competition보다 먼저 생성되므로 `Meeting.competitionId`가 생성 흐름에 맞다.
- 한 모임당 하나의 경기표만 허용하므로 `COMPETITION_ID unique` 제약을 둔다.
- 참석 응답과 경기 참가자를 분리해 `NOT_ATTENDING`, `MAYBE`, 이름 기반 응답이 매칭 도메인을 오염시키지 않게 한다.
- 참석 응답 단계에서 성별을 필수로 받아 경기표 생성 시 모임장이 별도 보완 작업 없이 바로 Competition을 만들 수 있게 한다.
- 정원 제한은 optional이며 `ATTENDING` 인원에만 적용한다. `NOT_ATTENDING`, `MAYBE`는 정원에 포함하지 않는다.
- 총 정원 방식과 성별 정원 방식은 상호 배타적이다. 총 정원이 있으면 총 참석 인원만 검증하고, 총 정원이 없을 때만 요청 성별의 정원을 검증한다.
- 정원 검증은 여러 MeetingAttendance의 count를 기반으로 하므로 Meeting row에 비관적 락을 걸어 같은 모임의 참석 변경 트랜잭션을 순차 처리한다.
- Meeting 기반 Competition 생성은 Meeting의 `courtCount`, `totalGames`, `title`, 참석자 이름/성별을 사용해 기존 Competition 생성 로직으로 위임한다.
- 기존 Competition 생성 API와 Competition 도메인 용어는 유지한다.

### API Scope

```text
POST /api/meetings
- 로그인 필요
- Meeting 생성

GET /api/meetings/{publicId}
- 공개 접근
- 참석 체크 화면용 Meeting 정보와 참석 요약 조회

GET /api/meetings/{publicId}/manage
- 로그인 필요
- owner 전용 상세 조회

GET /api/me/meetings
- 로그인 필요
- 내가 만든 Meeting 목록 조회
- title, startAt, endAt, status, 참석 요약, competition 연결 여부 반환

PATCH /api/meetings/{publicId}
- 로그인 필요
- owner만 가능
- title, startAt, endAt, note, maxParticipants, maxMaleParticipants,
  maxFemaleParticipants, courtCount, totalGames 수정
- Competition 생성 후에는 courtCount, totalGames 수정 제한

PATCH /api/meetings/{publicId}/status
- 로그인 필요
- owner만 가능
- OPEN, CLOSED, CANCELLED 변경

DELETE /api/meetings/{publicId}
- 로그인 필요
- owner만 가능
- soft delete

POST /api/meetings/{publicId}/attendances
- 공개 접근
- 이름/성별/참석 상태로 응답 생성 또는 이름 선택 기반 수정
- 이름 선택 기반 수정 시 이름, 성별, 참석 상태 모두 수정 가능
- Meeting에 Competition이 연결되어 있으면 실패

PATCH /api/meetings/{publicId}/attendances/{attendanceId}
- 로그인 필요
- owner만 가능
- 참석자 이름, 성별, 참석 상태 수정
- 정원 제한은 owner 수정에도 동일하게 적용
- Meeting에 Competition이 연결되어 있으면 실패

DELETE /api/meetings/{publicId}/attendances/{attendanceId}
- 로그인 필요
- owner만 가능
- 참석 응답 soft delete
- Meeting에 Competition이 연결되어 있으면 실패

POST /api/meetings/{publicId}/competition
- 로그인 필요
- owner만 가능
- ATTENDING 응답으로 Competition 생성 후 Meeting.competitionId 연결
- 응답은 생성된 Competition의 `publicId`만 반환한다. Meeting 기반 생성은 owner가 이미 확정되어 `competitionAdminToken`을 반환하지 않는다.
- 기존 Competition 생성 validation을 그대로 사용

DELETE /api/meetings/{publicId}/competition
- 로그인 필요
- owner만 가능
- 연결된 Competition soft delete 후 Meeting.competitionId null 처리
- 이후 같은 Meeting에서 Competition 재생성 가능
```

## 5. Plan / Commit Units

- [x] `feat(meeting): add meeting persistence` - done, verified
  - 구현:
    - `com.tennisfolio.Tennisfolio.meeting.entity.Meeting`
    - `com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance`
    - `MeetingRepository`, `MeetingAttendanceRepository`
  - 테스트 설명:
    - Meeting 생성 시 publicId, ownerUserId, status 기본값이 저장되는지 검증한다.
    - MeetingAttendance 생성 시 meeting, 이름, 성별, 참석 상태가 저장되는지 검증한다.
  - 검증 기준:
    - Meeting 엔티티/저장소 단위 테스트가 통과한다.

- [x] `feat(meeting): add meeting APIs` - done, verified
  - 구현:
    - `MeetingController`
    - `MeetingCreateRequest/Response`
    - `MeetingDetailResponse`
    - `MeetingCommandService`, `MeetingQueryService`
  - 테스트 설명:
    - 로그인 사용자는 모임을 만들 수 있다.
    - 로그인 사용자는 자신이 만든 모임 목록을 조회할 수 있다.
    - owner는 모임을 수정, 삭제, 마감, 재오픈할 수 있다.
    - owner가 아닌 사용자는 관리 API를 사용할 수 없다.
    - 비로그인 사용자는 publicId로 모임을 조회할 수 있다.
    - 종료 시간이 시작 시간보다 빠르거나 같으면 모임 생성이 실패한다.
    - 코트 수와 경기 수가 1 미만이면 모임 생성이 실패한다.
    - Competition 생성 후에는 모임의 경기 생성 조건을 바꿀 수 없다.
  - 검증 기준:
    - `MeetingControllerTest`, `MeetingCommandServiceTest`, `MeetingQueryServiceTest`의 Meeting CRUD 케이스가 통과한다.

- [x] `feat(meeting): add attendance APIs` - done, verified
  - 구현:
    - `MeetingAttendanceUpsertRequest/Response`
    - 공개 참석 응답 생성/수정 API
    - owner 참석자 수정/삭제 API
    - 참석 정원 검증
  - 테스트 설명:
    - 비로그인 사용자는 이름과 성별로 참석 체크할 수 있다.
    - 공개 참석 화면에서 사용자는 기존 응답을 선택해 이름, 성별, 참석 상태를 모두 수정할 수 있다.
    - owner는 참석 응답의 이름, 성별, 참석 상태를 수정하거나 삭제할 수 있다.
    - 총 정원 또는 성별 정원이 찬 경우 해당 참석자가 `ATTENDING`을 선택할 수 없다.
    - owner가 참석자를 `ATTENDING`으로 변경할 때도 총 정원과 성별 정원을 검증한다.
    - Competition 생성 후에는 공개 참석 수정과 owner 참석자 수정/삭제가 실패한다.
    - 이름 누락, 성별 누락, 중복 이름, 닫힌 모임 참석 체크 실패를 검증한다.
  - 검증 기준:
    - `MeetingAttendanceCommandServiceTest` 관련 케이스가 통과한다.

- [ ] `feat(meeting): create competition from meeting attendees`
  - 구현:
    - `POST /api/meetings/{publicId}/competition`
    - `DELETE /api/meetings/{publicId}/competition`
    - 참석자 중 `ATTENDING` 상태만 CompetitionEntry 후보로 사용한다.
    - Meeting의 코트 수와 경기 수를 Competition 생성 요청에 사용한다.
    - 기존 Competition 생성 validation을 재사용한다.
    - Competition 생성 성공 후 `Meeting.competitionId`만 연결하고, 참석 응답과 CompetitionEntry는 독립 스냅샷으로 유지한다.
    - Meeting 기반 경기표 삭제 시 Competition은 soft delete하고 Meeting의 Competition 연결만 해제한다.
  - 테스트 설명:
    - 모임 owner만 경기표를 생성할 수 있다.
    - 이미 Competition이 연결된 Meeting은 중복 생성할 수 없다.
    - 모임 owner는 연결된 Competition을 삭제할 수 있고, 삭제 후 Meeting.competitionId가 null이 된다.
    - 연결된 Competition 삭제 후 같은 Meeting에서 Competition을 다시 생성할 수 있다.
    - 연결된 Competition 삭제 후 참석 응답을 다시 수정할 수 있다.
    - Meeting 없이 기존 `/api/competitions` 생성은 깨지지 않는다.
  - 검증 기준:
    - `MeetingCompetitionCreateServiceTest`와 기존 `CompetitionCommandServiceTest` 관련 케이스가 통과한다.

- [ ] `feat(meeting): add meeting UI flow`
  - 구현:
    - `/meetings` 내 모임 목록 화면
    - `/meetings/new` 모임 만들기 화면
    - `/meetings/:publicId` 공개 참석 체크 화면
    - 참석 응답 팝업
    - 모임장용 참석자 목록과 경기표 생성 액션
    - `/meetings/:publicId/manage` 모임장 관리 화면
    - 생성된 Competition으로 이동하는 링크
  - 테스트 설명:
    - 공개 화면에서 이름과 성별 입력 후 참석 상태를 저장하는 흐름을 검증한다.
    - 모임장이 참석자를 확인하고 경기표 생성 액션에 도달하는 UI 상태를 검증한다.
  - 검증 기준:
    - 관련 프론트 단위 테스트 또는 수동 검증 결과를 Verification Log에 기록한다.

## 6. Development Validation

- [ ] 성공 흐름: 로그인 사용자 모임 생성 -> 비로그인 이름/성별 참석 체크 -> 경기표 생성 -> Competition 상세 이동
- [ ] 성공 흐름: 로그인 사용자 내 모임 목록 조회
- [ ] 성공 흐름: 공개 참석 화면에서 기존 응답의 이름/성별/참석 상태 수정
- [ ] 실패 흐름: 비로그인 모임 생성 불가
- [ ] 실패 흐름: owner가 아닌 사용자의 모임 수정/삭제/상태 변경 실패
- [ ] 실패 흐름: owner가 아닌 사용자의 참석자 수정/삭제 실패
- [ ] 실패 흐름: 빈 이름, 성별 누락, 중복 이름 참석 체크 실패
- [ ] 실패 흐름: 총 정원 또는 성별 정원이 찬 경우 참석 선택 실패
- [ ] 실패 흐름: 마감 또는 삭제된 모임 참석 체크 실패
- [ ] 권한: Meeting owner가 아닌 사용자는 Meeting 기반 Competition 생성 불가
- [ ] 권한: Meeting owner가 아닌 사용자는 Meeting 기반 Competition 삭제 불가
- [ ] 실패 흐름: Competition 생성 후 참석 응답 생성/수정/삭제 실패
- [ ] 성공 흐름: Meeting 기반 Competition 삭제 후 같은 Meeting에서 경기표 재생성 가능
- [ ] 성공 흐름: Meeting 기반 Competition 삭제 후 참석 응답 수정 가능
- [ ] DB: Meeting 없이 Competition 생성 가능, Competition 없이 Meeting 생성 가능
- [ ] FE/BE 계약: API request/response 필드명이 UI 사용처와 일치
- [ ] 관련 테스트 또는 승인된 검증 명령 결과를 Verification Log에 기록

## 7. Verification Commands

프로젝트 규칙상 Java/Gradle/npm 테스트와 빌드는 명시 승인을 받은 경우에만 실행한다.

```text
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.*
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest
node --test src/tennisFolio/src/**/*.test.mjs
```

## 8. Final QA Checklist

- [ ] 사용자가 처음부터 끝까지 모임 참석 체크 흐름을 완료할 수 있다.
- [ ] 실패 시 다음 행동을 이해할 수 있는 메시지가 보인다.
- [ ] 기존 Competition 생성/상세/클럽 세션 흐름이 깨지지 않는다.
- [ ] 테스트 결과 또는 수동 검증 결과가 PR 설명에 포함된다.
- [ ] 필요하면 gstack `/qa`로 최종 사용자 흐름을 검증한다.

## 9. Verification Log

| Date | Command / Check | Result | Notes |
|---|---|---|---|
| 2026-06-25 | 문서 작성 전 코드 구조 확인 | PASS | `Competition`, `CompetitionEntry`, `CompetitionCommandService`, `CompetitionController` 확인 |
| 2026-06-26 | `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepositoryTest` | PASS | Meeting/MeetingAttendance 저장 동작 확인 |
| 2026-06-26 | `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.*` | PASS | Meeting 생성/조회/수정/삭제/상태 변경 API와 서비스 검증 |
| 2026-06-26 | `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.*` | PASS | 참석 응답 생성/수정/삭제, 중복 이름, 마감/Competition 연결, 정원 검증 |
| 2026-06-26 | `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.*` | PASS | 참석 변경 시 Meeting 비관적 락 조회 사용과 기존 Meeting 기능 회귀 확인 |
| 2026-06-26 | `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.*` | PASS | 총 정원/성별 정원 상호 배타 validation과 정원 방식 선택 검증 |
| 2026-06-27 | 코드 변경: Meeting 기반 Competition 생성/삭제 API와 테스트 추가 | SKIPPED | 프로젝트 규칙상 명시 승인 없이 Gradle 테스트를 실행하지 않음 |

## 10. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-06-25 | 초기 작성 | Meeting 참석 체크 기능 개발 시작 |
| 2026-06-27 | Meeting 기반 Competition 생성/삭제 API 구현 | 참석자 확정 후 기존 Competition 경기표 흐름으로 연결 |
