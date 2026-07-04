# Feature: 모임 참석 체크

## 1. Goal

로그인한 사용자가 테니스 모임을 만들고 링크를 공유하면, 비로그인 사용자도 이름과 성별을 입력해 참석 여부를 남길 수 있게 한다.
참석자가 확정되면 모임장은 참석자 목록으로 한 모임당 하나의 Competition을 생성해 기존 경기표/클럽 세션 운영 흐름으로 이어간다.

## 2. User Flow

```text
로그인 사용자 -> 모임 만들기 -> 공유 링크 확인
링크 방문자 -> 이름과 성별 입력 -> 참석/불참/대기 선택 -> 응답 저장
모임장 -> 참석자 목록 확인 -> 경기표 생성
시스템 -> Competition 생성 -> Meeting.competitionId 연결 -> 경기표 보기 제공
```

## 3. Spec

- 사용자 화면 용어는 `모임`을 사용하고, 코드/API/DB 도메인은 `Meeting`을 사용한다.
- 모임 생성은 로그인 사용자만 가능하다. 생성자의 user id를 `Meeting.ownerUserId`로 저장한다.
- 모임 owner는 모임 정보를 조회, 수정, 삭제할 수 있다.
- 모임 owner는 참석 응답의 이름, 성별, 참석 상태를 수정하거나 참석 응답을 삭제할 수 있다.
- 공개 참석 화면에서도 사용자는 기존 참석 응답을 선택해 이름, 성별, 참석 상태를 모두 수정할 수 있다.
- 로그인한 사용자가 공개 참석 화면에서 참석하는 경우 이름은 프로필 nickname으로 고정하고, 생성자 참석 이름과 같이 화면에서 수정할 수 없게 한다.
- 공개 참석 화면은 사용자가 선택하거나 저장한 참석 응답을 브라우저 `localStorage`에 모임별로 기억할 수 있다.
- 같은 브라우저에서 같은 공개 참석 링크를 다시 열면 저장된 참석 응답이 현재 명단에 존재하는 경우 바로 상세 화면으로 진입한다.
- 저장된 참석 응답이 삭제됐거나 현재 모임 명단에 없으면 기억 값을 무시하고 최초 입장 화면을 보여준다.
- 공개 참석 화면 상세에는 다른 참석자로 다시 입장할 수 있는 액션을 제공하고, 이 액션은 해당 모임의 브라우저 기억 값을 삭제한다.
- 참석 체크는 로그인 없이 가능하며, 이름과 성별은 필수이다.
- 참석 상태는 `ATTENDING`, `NOT_ATTENDING`, `WAITING`를 지원한다.
- 한 모임에서는 같은 이름의 활성 참석 응답을 중복 생성하지 않는다.
- 한 모임은 최대 하나의 Competition만 연결한다.
- Meeting은 `competitionId`만 저장하고, Competition은 Meeting id를 저장하지 않는다.
- Meeting 상세 응답은 연결된 Competition이 있으면 화면 이동을 위해 `competitionPublicId`를 함께 반환한다.
- Meeting 공개 화면은 `competitionPublicId`가 있을 때 `경기표 보기` 액션으로 Competition 상세 화면으로 이동할 수 있다.
- Meeting 관리 화면은 운영 관리에서 `대진표 생성`을 주요 액션으로 보여주고, 생성 후 `대진표 보기`로 Competition 상세 화면으로 이동할 수 있다.
- 모임 시간은 시작 시간과 종료 시간을 입력한다. 종료 시간은 시작 시간 이후여야 한다.
- 모임은 총 참석 정원 방식 또는 성별 참석 정원 방식 중 하나만 사용할 수 있다.
- 총 참석 정원을 설정하면 남자/여자 참석 정원은 설정하지 않는다.
- 남자/여자 참석 정원을 설정하면 총 참석 정원은 설정하지 않는다.
- `ATTENDING`으로 저장하거나 변경할 때 선택된 정원 방식의 정원이 이미 찼으면 선택할 수 없다.
- 참석 응답 생성/수정/삭제는 Meeting row 비관적 쓰기 락을 잡고 처리해 같은 모임의 정원 검증과 중복 이름 검사를 직렬화한다.
- 모임은 Competition 생성을 위해 코트 수와 경기 수를 저장한다.
- 모임 삭제는 soft delete로 처리한다.
- 모임 owner는 참석 체크를 마감하거나 다시 열 수 있다. 마감된 모임은 새 참석 응답과 참석 상태 변경을 받지 않는다.
- 모임 owner가 참석 응답이나 연결된 Competition을 삭제할 때는 어두운 배경의 중앙 확인 팝업으로 사용자 확인을 받는다.
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
  - 공개 참석 화면은 `meetingPublic:{publicId}:attendance` localStorage key에 `{ attendanceId, participantName }`를 저장해 같은 브라우저의 재방문자를 식별한다.
  - 공개 참석 화면은 서버에서 모임과 참석 명단을 조회한 뒤 localStorage의 `attendanceId`가 현재 명단에 있을 때만 자동 진입시킨다.
- 자동 진입은 인증이나 권한으로 취급하지 않고, 사용 편의를 위한 브라우저별 기억으로만 사용한다.
- `/meetings/:publicId/manage`는 owner 전용 관리 화면이다. owner는 이 화면에서 모임 정보 수정, 참석자 관리, 참석 마감/재오픈, 모임 삭제, 대진표 생성/삭제를 수행한다.
- 관리 화면의 운영 관리는 대진표 생성/보기, 공유/수정, 참석 체크 상태 제어를 시각적으로 분리하고, 모임 삭제는 불참 명단 아래 위험 작업 영역에 둔다.
- Meeting에서 생성된 경기표로 이동하는 링크는 Meeting 상세 응답의 `competitionPublicId`로 구성하며, Competition에서 Meeting으로 돌아가는 역방향 링크는 만들지 않는다.

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
- 모임 정보, 참석/대기/불참 명단, 남/녀/총 참석 카운트, 정원 상태 표시
- 최초 입장 화면과 상세 화면 모두 제한 인원수와 현재 참석 인원을 표시
- 참석 응답 팝업에서 이름, 성별, 참석 여부 입력 또는 기존 응답 수정

/meetings/:publicId/manage
- 모임장 관리 화면
- 로그인 owner만 접근
- 기본 정보 수정, 참석자 수정/삭제, 참석 마감/재오픈, 공유 링크 복사, 대진표 생성/삭제, 모임 삭제
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
- ATTENDANCE_STATUS not null: ATTENDING, NOT_ATTENDING, WAITING
- GENDER not null: MALE, FEMALE
- DEL_DT nullable
- CREATE_DT / UPDATE_DT
```

### Review Questions

- Meeting과 Competition의 owner는 각각의 리소스 소유자이므로 중복이 아니라 독립 권한 기준이다.
- Meeting은 Competition보다 먼저 생성되므로 `Meeting.competitionId`가 생성 흐름에 맞다.
- 한 모임당 하나의 경기표만 허용하므로 `COMPETITION_ID unique` 제약을 둔다.
- 참석 응답과 경기 참가자를 분리해 `NOT_ATTENDING`, `WAITING`, 이름 기반 응답이 매칭 도메인을 오염시키지 않게 한다.
- 참석 응답 단계에서 성별을 필수로 받아 경기표 생성 시 모임장이 별도 보완 작업 없이 바로 Competition을 만들 수 있게 한다.
- 정원 제한은 optional이며 `ATTENDING` 인원에만 적용한다. `NOT_ATTENDING`, `WAITING`는 정원에 포함하지 않는다.
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
- Competition이 연결된 경우 `competitionPublicId` 반환

GET /api/meetings/{publicId}/manage
- 로그인 필요
- owner 전용 상세 조회
- Competition이 연결된 경우 `competitionPublicId` 반환

GET /api/me/meetings
- 로그인 필요
- 내가 만든 Meeting 목록 조회
- title, startAt, endAt, courtCount, totalGames, status, 참석/대기/불참 요약, competition 연결 여부 반환

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

- [x] `feat(meeting): add meeting UI routes and API client` - done, verified
  - 구현:
    - `App.jsx`에 `/meetings`, `/meetings/new`, `/meetings/:publicId`, `/meetings/:publicId/manage` 라우트를 추가한다.
    - `utils/meetingApi.js`를 추가해 Meeting API 호출을 한 곳에 모은다.
    - API 함수는 `getMyMeetings`, `createMeeting`, `getPublicMeeting`, `getManagedMeeting`, `updateMeeting`, `updateMeetingStatus`, `deleteMeeting`, `upsertAttendance`, `updateAttendance`, `deleteAttendance`, `createMeetingCompetition`, `deleteMeetingCompetition`으로 나눈다.
    - Meeting UI는 `competition-theme.css` 변수를 사용하고 새 hard-coded Competition 색상을 추가하지 않는다.
  - 테스트 설명:
    - API client 함수가 문서화된 endpoint와 HTTP method를 사용하는지 단위 테스트로 검증한다.
    - 라우트가 의도한 페이지 컴포넌트로 연결되는지 렌더링 가능한 수준에서 확인한다.
  - 검증 기준:
    - `node --test` 기반 관련 테스트가 통과하거나, 프로젝트 규칙상 실행하지 못한 경우 실행 생략 사유를 Verification Log에 기록한다.

- [ ] `feat(meeting): add my meetings list UI`
  - 구현:
    - `/meetings`에서 로그인 사용자가 만든 모임 목록을 조회한다.
    - 각 모임 카드에는 제목, 날짜/시간, 코트 수, 경기 수, 참석/대기/불참 카운트, 경기표 생성 여부를 표시한다.
    - 목록 상단에는 `모임 만들기` 진입 버튼을 둔다.
    - 각 모임에서 `관리`, `공유`, `삭제` 액션을 제공한다.
    - 삭제는 확인 후 owner 삭제 API를 호출하고 목록에서 제거한다.
  - 테스트 설명:
    - 목록 응답이 비어 있으면 빈 상태가 보인다.
    - 목록 응답이 있으면 모임 정보와 관리/공유 액션이 보인다.
    - 삭제 성공 시 해당 모임이 목록에서 사라지고 성공 메시지가 보인다.
  - 검증 기준:
    - 내 모임 목록을 열고 관리 화면으로 이동할 수 있다.

- [ ] `feat(meeting): add meeting create wizard UI`
  - 구현:
    - `/meetings/new`에 목업 기준 2단계 생성 폼을 만든다.
    - 1단계는 제목, 날짜, 시작/종료 시간, note를 입력한다.
    - 2단계는 정원 방식 없음/총 정원/성별 정원, 코트 수, 총 경기 수를 입력한다.
    - 총 정원과 성별 정원 입력은 선택한 정원 방식에 맞는 필드만 노출한다.
    - 생성 성공 시 `/meetings/{publicId}/manage`로 이동한다.
  - 테스트 설명:
    - 필수값 누락, 종료 시간이 시작 시간보다 빠르거나 같은 경우 생성 버튼을 막거나 오류를 표시한다.
    - 정원 방식 변경 시 상호 배타 필드가 올바르게 표시된다.
    - 생성 성공 후 관리 화면 경로로 이동한다.
  - 검증 기준:
    - 로그인 사용자가 목업의 2단계 흐름으로 모임을 만들 수 있다.

- [ ] `feat(meeting): add public attendance UI`
  - 구현:
    - `/meetings/:publicId`에서 비로그인 사용자가 공개 모임 정보를 볼 수 있게 한다.
    - 참석/대기/불참 카운트와 성별별 참석 명단을 표시한다.
    - 최초 입장 화면과 상세 화면 모두 제한 인원수와 현재 참석 인원을 표시한다.
    - 이름, 성별, 참석 상태를 입력해 참석 응답을 생성하거나 기존 응답을 수정할 수 있는 폼 또는 팝업을 제공한다.
    - 참석 응답 저장 성공 또는 기존 이름 선택 시 `meetingPublic:{publicId}:attendance` localStorage key에 `attendanceId`와 `participantName`을 저장한다.
    - 공개 모임 조회 후 localStorage에 저장된 `attendanceId`가 현재 참석 명단에 있으면 해당 참석 응답을 `form`에 채우고 상세 화면으로 자동 전환한다.
    - localStorage에 저장된 `attendanceId`가 현재 참석 명단에 없거나 파싱할 수 없으면 저장 값을 삭제하고 최초 입장 화면을 유지한다.
    - 상세 화면에 `다른 이름으로 입장` 액션을 제공하고, 클릭 시 해당 모임의 localStorage 기억 값을 삭제한 뒤 최초 입장 화면으로 돌아간다.
    - 마감, 삭제, 경기표 생성 완료, 정원 초과 실패 메시지를 사용자가 다음 행동을 알 수 있게 표시한다.
    - 공유 링크 복사 액션을 제공한다.
    - `competitionPublicId`가 있으면 `경기표 보기` 액션을 제공한다.
  - 테스트 설명:
    - 공개 조회 성공 시 모임 정보와 참석 현황이 보인다.
    - 최초 입장 화면과 상세 화면에 총 정원 또는 성별 정원이 보인다.
    - 이름/성별/참석 상태 저장 성공 후 최신 참석 현황이 반영된다.
    - 참석 응답 저장 성공 또는 기존 이름 선택 시 모임별 localStorage key가 저장된다.
    - 같은 공개 링크 재방문 시 저장된 `attendanceId`가 현재 명단에 있으면 상세 화면으로 자동 진입한다.
    - 저장된 `attendanceId`가 현재 명단에 없으면 localStorage 값을 지우고 최초 입장 화면을 보여준다.
    - `다른 이름으로 입장` 클릭 시 localStorage 값을 지우고 최초 입장 화면으로 전환한다.
    - `competitionPublicId`가 있으면 `경기표 보기` 액션이 `/competitions/{competitionPublicId}`로 이동한다.
    - 정원 초과나 마감 상태에서는 API 오류 메시지가 표시된다.
  - 검증 기준:
    - 비로그인 사용자가 공개 링크에서 참석 상태를 남기고 수정할 수 있으며, 같은 브라우저에서는 이전에 선택한 참석자로 이어서 접속할 수 있다.

- [ ] `feat(meeting): add meeting owner manage UI`
  - 구현:
    - `/meetings/:publicId/manage`에서 owner 전용 상세 정보를 조회한다.
    - 모임 정보 수정, 참석 마감/재오픈, 공유 링크 복사, 모임 삭제를 제공한다.
    - owner는 참석자의 이름, 성별, 참석 상태를 수정하거나 삭제할 수 있다.
    - `ATTENDING` 참석자로 대진표 생성 액션을 제공하고, 성공 시 생성된 Competition 상세로 이동하는 링크를 보여준다.
    - 연결된 Competition 삭제 액션을 제공하고 삭제 후 참석 응답 수정이 다시 가능하도록 화면 상태를 갱신한다.
    - 연결된 Competition이 있으면 `대진표 보기` 액션을 제공한다.
    - 모임 삭제 액션은 불참 명단 아래 위험 작업 영역에 둔다.
  - 테스트 설명:
    - owner 상세 조회 성공 시 참석자 목록과 owner 액션이 보인다.
    - 참석자 수정/삭제 성공 시 목록과 카운트가 갱신된다.
    - 대진표 생성 성공 시 Competition 링크가 보인다.
    - 관리 화면의 `대진표 보기` 액션이 `/competitions/{competitionPublicId}`로 이동한다.
    - 대진표 삭제 성공 시 Meeting의 competition 연결 상태가 해제되어 보인다.
  - 검증 기준:
    - 모임장이 참석자를 확정하고 경기표 생성/삭제 흐름까지 관리할 수 있다.

## 6. Development Validation

- [ ] 성공 흐름: 로그인 사용자 모임 생성 -> 비로그인 이름/성별 참석 체크 -> 경기표 생성 -> Competition 상세 이동
- [ ] 성공 흐름: 로그인 사용자 내 모임 목록 조회
- [ ] 성공 흐름: 공개 참석 화면에서 기존 응답의 이름/성별/참석 상태 수정
- [ ] 성공 흐름: 같은 브라우저에서 공개 참석 링크 재방문 시 이전에 선택한 참석자로 상세 화면 자동 진입
- [ ] 성공 흐름: 공개 참석 상세에서 다른 이름으로 입장해 브라우저 기억 값 삭제
- [ ] 실패 흐름: 브라우저에 저장된 참석 응답이 현재 명단에 없으면 최초 입장 화면 표시
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
| 2026-06-29 | `node --test src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | Meeting UI 라우트와 Meeting API client endpoint 계약 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingManage.test.mjs` | PASS | `/meetings/{publicId}/manage` mockup 대시보드 패널, roster, 삭제 확인 UI 계약 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 내 정보 nickname 표시와 상태 클릭 즉시 저장 계약 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 note prefix, 수정 안내 문구, 운영 메모 제거 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 desktop/mobile 공통 1열 레이아웃 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 desktop/mobile 공통 520px 폭 검증 |

| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 참석 화면 mockup 카드, 내 정보/상태 즉시 저장, 성별/상태별 명단 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 참석 화면 최초 입장 화면과 저장/이름 선택 후 상세 전환 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 상세 화면에서 저장 응답 id를 보존해 내 정보 이름 변경이 기존 응답 수정으로 처리되는지 검증 |
| 2026-06-29 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 상세 화면 참석자 명단을 읽기 전용으로 유지해 비-owner가 다른 참석 응답을 선택 수정하지 못하는지 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 참석 화면 localStorage 기억, 재방문 자동 진입, 다른 이름 입장 계약 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingToast.test.mjs src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | Meeting 화면 알림을 단일 toast로 통일하고 3초 자동 닫힘 계약 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingToast.test.mjs src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 참석자 삭제와 연결된 경기표 삭제를 중앙 확인 팝업으로 표시하는 계약 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs` | PASS | Meeting 공개/관리 화면의 `경기표 보기` 액션이 `competitionPublicId`로 Competition 상세에 이동하는 계약 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingToast.test.mjs src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 관리 화면 운영 관리를 대진표 생성 중심으로 재배치하고 모임 삭제를 불참 명단 아래로 분리한 계약 검증 |
| 2026-06-30 | `node --test src\tennisFolio\src\page\MeetingToast.test.mjs src\tennisFolio\src\page\MeetingPublic.test.mjs src\tennisFolio\src\page\MeetingManage.test.mjs src\tennisFolio\src\page\Meetings.test.mjs src\tennisFolio\src\utils\meetingApi.test.mjs` | PASS | 공개 참석 최초 입장 화면과 상세 화면에 제한 인원수와 현재 참석 인원 표시 계약 검증 |
| 2026-06-30 | Gradle 테스트 | SKIPPED | 프로젝트 규칙상 명시 승인 없이 Java/Gradle 테스트를 실행하지 않음 |

## 10. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-06-25 | 초기 작성 | Meeting 참석 체크 기능 개발 시작 |
| 2026-06-27 | Meeting 기반 Competition 생성/삭제 API 구현 | 참석자 확정 후 기존 Competition 경기표 흐름으로 연결 |
| 2026-06-30 | 공개 참석 화면 localStorage 기억 요구사항 추가 | 같은 공개 링크 재방문 시 선택한 참석자로 이어서 접속하는 UX 정의 |
| 2026-06-30 | 관리 화면 삭제 확인 팝업 요구사항 추가 | 참석자 삭제와 연결된 경기표 삭제가 화면 하단 패널 대신 중앙 확인 팝업을 사용하도록 정의 |
| 2026-06-30 | Meeting에서 Competition으로 이동하는 단방향 링크 요구사항 추가 | DB 연결은 `Meeting.competitionId`만 유지하고 화면 이동용 `competitionPublicId`만 응답으로 조립 |
