# Feature: Club Meeting MVP

## 1. Goal

클럽 ADMIN이 클럽 상세 화면에서 모임을 만들고, 링크를 받은 사용자가 해당 모임에 참석 응답을 남길 수 있게 한다.
기존 `Meeting` 도메인의 참석 체크, 정원, 경기표 생성 흐름은 최대한 재사용하되, 모임이 특정 `Club`에 소속된다는 연결과 권한 규칙을 추가한다.

1차 범위는 클럽 상세의 `모임` 탭을 실제 데이터로 연결하고, 클럽 안에서 모임 생성/조회/수정/삭제/참석 체크/경기표 생성까지 이어지게 하는 것이다.

## 2. User Flow

```text
클럽 ADMIN -> 클럽 상세 -> 모임 탭 -> 모임 추가 -> 클럽 모임 생성
사용자 -> 공유 링크 접속 -> 모임 선택 -> 참석/대기/불참 응답
클럽 ADMIN -> 모임 관리 -> 참석자 확인 -> 경기표 생성
시스템 -> 기존 Competition 생성 -> Meeting.competitionId 연결 -> 경기표 보기 제공
```

## 3. Spec

- 사용자 화면 용어는 `클럽 모임` 또는 `모임`을 사용하고, 코드/API/DB 도메인은 기존 `Meeting`을 확장한다.
- 클럽 모임은 하나의 `Club`에 소속된다.
- 클럽 모임 생성은 해당 클럽의 `ADMIN`만 가능하다.
- 클럽 상세의 클럽 모임 목록 조회는 해당 클럽의 활성 클럽원만 가능하다.
- 클럽 모임 자체는 링크를 알면 누구나 공개 모임 화면에서 조회할 수 있다.
- 클럽 모임 수정, 삭제, 상태 변경, 참석자 관리, 경기표 생성/삭제는 해당 클럽의 `ADMIN`만 가능하다.
- 참석 응답은 기존 공개 모임과 동일하게 링크를 가진 사용자가 남길 수 있다.
- 1차 범위에서는 링크 방문자가 직접 참석 응답을 남긴다. ADMIN이 클럽원을 대신 참석 등록하는 기능은 후순위로 둔다.
- 클럽 모임 참석 응답은 기존 `MeetingAttendance`를 재사용한다.
- 공개 링크 화면에서는 클럽원 검색이나 클럽원 목록을 제공하지 않는다.
- 로그인한 사용자가 해당 클럽의 활성 클럽원이면 참석 폼을 그 클럽원 정보로 자동 채우고, 저장 시 `participantType=CLUB_MEMBER`, `clubMemberId`를 저장한다.
- 비로그인 사용자 또는 해당 클럽원이 아닌 로그인 사용자는 이름과 성별을 직접 입력한다.
- 직접 입력한 이름과 성별이 해당 클럽의 활성 클럽원 1명과 정확히 일치하면 `participantType=CLUB_MEMBER`, `clubMemberId`를 저장한다.
- 직접 입력한 이름과 성별이 클럽원과 일치하지 않거나, 여러 명과 일치하면 `participantType=GUEST`로 저장한다.
- 참석 명단에는 `클럽원` 또는 `게스트` 배지를 표시한다.
- 참석 입력 폼에는 "이름과 성별이 클럽원 정보와 일치하면 클럽원으로 표시됩니다." 정도의 짧은 안내만 표시한다.
- 기존 공개 모임(`/meetings/{publicId}`)은 계속 비로그인 참석 링크로 동작해야 한다.
- 클럽 모임의 공개 참석 화면은 기존 공개 링크 기반 자동 진입(localStorage 기억)을 재사용할 수 있다.
- 기존 `Meeting`의 정원 규칙, 상태 규칙, Competition 생성 규칙은 유지한다.
- 경기표 생성 후에는 기존 Meeting 규칙과 동일하게 참석 응답 생성/수정을 막는다.
- 클럽 삭제 시 연결된 클럽 모임 처리 정책은 1차에서 명시적으로 정한다. 기본 방향은 클럽 soft delete와 함께 클럽 화면에서 모임을 숨기고, Meeting 데이터는 soft delete하거나 조회 불가 상태로 둔다.
- 클럽원 삭제 시 기존 참석 응답을 즉시 삭제하지 않는다. 이미 남긴 RSVP는 모임 기록으로 남긴다.

## 4. Design Review

### Responsibility

- Controller / API:
  - `ClubMeetingController` 또는 `MeetingController` 확장으로 `/api/clubs/{clubPublicId}/meetings` 계열 API를 제공한다.
  - 기존 `/api/meetings` 공개/개인 모임 API는 그대로 유지한다.
- Service / Application:
  - `ClubAccessService`로 클럽원/ADMIN 권한을 검증한다.
  - 기존 `MeetingCommandService`, `MeetingQueryService`, `MeetingAttendanceCommandService`, `MeetingCompetitionCreateService`는 재사용 가능한 부분을 유지한다.
  - 클럽 권한과 `clubPublicId` 스코프 검증은 별도 클럽 모임 서비스에서 감싼다.
- Domain:
  - `Meeting`에 `clubId`를 nullable로 추가하는 방향을 우선 검토한다.
  - `clubId == null`이면 기존 독립 모임이고, `clubId != null`이면 클럽 모임이다.
  - 클럽 모임 참석자 구분을 위해 `MeetingAttendance`에 `participantType`과 nullable `clubMemberId`를 추가하는 방향을 우선 검토한다.
  - `participantType`은 `CLUB_MEMBER`, `GUEST`를 지원한다.
- Repository / Persistence:
  - 클럽별 모임 목록 조회를 위해 `MeetingRepository`에 `clubId` 기반 조회를 추가한다.
  - 기존 owner 기반 `/api/me/meetings` 조회는 독립 모임과 클럽 모임을 함께 보여줄지, 독립 모임만 보여줄지 결정해야 한다.
- Security / External:
  - 클럽 모임 API는 로그인 필수이다.
  - 클럽원 여부와 ADMIN 여부는 `ClubAccessService`가 판단한다.
- Frontend / UI:
  - 클럽 상세 `모임` 탭에서 실제 클럽 모임 목록을 조회한다.
  - 클럽 ADMIN에게만 `모임 추가` 액션을 보여준다.
  - 클럽 모임 생성 화면은 `docs/features/club/club-ui-mockup.html`의 `/clubs/{publicId}/meetings/new` 흐름을 기준으로 한다.
  - 기존 meeting 화면 컴포넌트를 재사용할 수 있으면 우선 재사용하되, 클럽 상세에서 돌아갈 위치와 권한 메시지는 클럽 맥락에 맞춘다.

### Review Questions

- 클럽 모임을 기존 `Meeting`에 `clubId` nullable 컬럼으로 붙이면 공개 모임과 클럽 모임이 같은 정원/참석/경기표 규칙을 공유할 수 있다.
- 별도 `ClubMeeting` 엔티티를 만들면 개념은 분리되지만 참석/Competition 생성 로직이 중복될 가능성이 크다.
- 1차 MVP에서는 `Meeting.ownerUserId`를 생성한 ADMIN으로 유지하고, 클럽 ADMIN 권한은 별도 검증으로 보강한다.
- 생성자가 클럽에서 강등되거나 삭제된 경우에도 다른 클럽 ADMIN이 모임을 관리할 수 있어야 한다.
- 클럽 모임 목록과 관리는 클럽원/ADMIN 권한을 적용하지만, 모임 참석 링크는 기존 공개 모임처럼 누구나 접근 가능하게 둔다.

## 5. API Scope

```text
GET /api/clubs/{clubPublicId}/meetings
- 로그인 필요
- 활성 클럽원만 가능
- 클럽 상세 모임 탭용 목록 조회
- title, publicId, startAt, endAt, status, 참석/대기/불참 요약, competition 연결 여부 반환

POST /api/clubs/{clubPublicId}/meetings
- 로그인 필요
- 클럽 ADMIN만 가능
- 기존 Meeting 생성 request를 재사용하되 clubId를 현재 Club으로 저장
- 응답은 생성된 meeting publicId 반환

GET /api/clubs/{clubPublicId}/meetings/{meetingPublicId}
- 로그인 필요
- 활성 클럽원만 가능
- 클럽 내부 상세 또는 관리 진입용 조회
- meeting이 해당 club에 속하지 않으면 404 또는 403 반환

GET /api/meetings/{meetingPublicId}
- 공개 접근
- 링크를 가진 사용자의 클럽 모임 상세와 참석 명단 조회
- 기존 공개 모임 조회 API를 재사용하되, clubId가 있는 Meeting도 조회 가능해야 함

PATCH /api/clubs/{clubPublicId}/meetings/{meetingPublicId}
- 로그인 필요
- 클럽 ADMIN만 가능
- 기존 Meeting 수정 규칙 유지

PATCH /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/status
- 로그인 필요
- 클럽 ADMIN만 가능
- OPEN, CLOSED, CANCELLED 변경

DELETE /api/clubs/{clubPublicId}/meetings/{meetingPublicId}
- 로그인 필요
- 클럽 ADMIN만 가능
- soft delete

POST /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/attendances
- 1차 범위에서는 사용하지 않는다.
- 참석 응답은 기존 공개 API인 `/api/meetings/{meetingPublicId}/attendances`를 재사용한다.
- 클럽 내부 참석 관리 API가 필요해지면 ADMIN 대리 등록 범위로 별도 추가한다.

POST /api/meetings/{meetingPublicId}/attendances
- 공개 접근
- 링크를 가진 사용자의 참석/대기/불참 응답 생성 또는 갱신
- 기존 공개 참석 API를 재사용하되, clubId가 있는 Meeting도 처리 가능해야 함
- 로그인한 활성 클럽원이면 서버가 해당 ClubMember로 연결한다.
- 비로그인 또는 비클럽원 입력은 이름과 성별로 활성 ClubMember 1명을 정확히 찾을 때만 ClubMember로 연결한다.
- 일치하는 ClubMember가 없거나 여러 명이면 Guest 참석자로 저장한다.
- 응답에는 참석자별 `participantType`과 화면 표시용 badge label을 포함한다.

PATCH /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/attendances/{attendanceId}
- 로그인 필요
- 클럽 ADMIN만 가능
- 참석자 이름, 성별, 참석 상태 수정

DELETE /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/attendances/{attendanceId}
- 로그인 필요
- 클럽 ADMIN만 가능
- 참석 응답 soft delete

POST /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/competition
- 로그인 필요
- 클럽 ADMIN만 가능
- 기존 Meeting 기반 Competition 생성 규칙 재사용

DELETE /api/clubs/{clubPublicId}/meetings/{meetingPublicId}/competition
- 로그인 필요
- 클럽 ADMIN만 가능
- 연결된 Competition soft delete 후 Meeting.competitionId 해제
```

## 6. Frontend Routes

```text
/clubs/:publicId
- 클럽 상세
- 모임 탭에서 클럽 모임 목록 표시
- ADMIN이면 모임 추가 버튼 표시

/clubs/:publicId/meetings/new
- 클럽 모임 생성
- ADMIN 전용
- 2단계 입력: 기본 정보 -> 모임 설정
- 저장 후 /clubs/:publicId/meetings/:meetingPublicId 또는 클럽 상세 모임 탭으로 이동

/clubs/:publicId/meetings/:meetingPublicId
- 클럽 내부 모임 상세 진입
- 클럽원 전용 목록에서 들어오는 화면이지만, 참석 자체는 공개 링크 화면을 재사용할 수 있음
- 참석 상태 선택, 참석자 요약, 경기표 보기 표시

/meetings/:meetingPublicId
- 공개 모임 참석 화면
- 클럽 모임도 링크를 알면 접근 가능
- 참석 상태 선택, 참석자 요약, 경기표 보기 표시
- 클럽 모임에서는 로그인한 클럽원의 이름/성별을 자동 채우고 `클럽원` 배지를 보여준다.
- 비로그인 사용자는 이름/성별을 입력하고, 저장 결과에 따라 `클럽원` 또는 `게스트` 배지가 표시된다.
- 공개 화면에서는 클럽원 검색 결과와 클럽원 목록을 노출하지 않는다.

/clubs/:publicId/meetings/:meetingPublicId/edit
- 클럽 모임 수정
- ADMIN 전용
- 기본 정보와 설정 수정, 모임 삭제 위험 작업 포함

/clubs/:publicId/meetings/:meetingPublicId/manage
- 필요하면 ADMIN 운영 화면으로 분리
- 참석자 관리, 마감/재오픈, 경기표 생성/삭제
```

## 7. Plan / Commit Units

- [x] `feat: add club meeting domain link`
  - 구현: `Meeting.clubId` nullable 컬럼 추가, repository 조회 추가, 기존 독립 Meeting 동작 유지
  - 테스트 설명: clubId가 있는 모임과 없는 모임이 각각 기존 조회/클럽 조회에서 의도대로 필터링되는지 검증
  - 검증 기준: 관련 meeting repository/service 테스트

- [x] `feat: expose club meeting api`
  - 구현: `/api/clubs/{clubPublicId}/meetings` 계열 API 추가, 클럽원/ADMIN 권한 검증
  - 테스트 설명: 클럽원 목록 조회 가능, 비클럽원 목록 조회 거부, MEMBER 생성/수정 거부, ADMIN 생성/수정/삭제 가능 검증
  - 검증 기준: club meeting controller/service 테스트

- [ ] `feat: connect club meeting frontend`
  - 구현: 클럽 상세 모임 탭 목록 API 연결, 모임 생성/상세/수정/관리 라우트 연결
  - 테스트 설명: 클럽 상세에서 모임 목록, 빈 상태, 생성 후 이동, 권한별 버튼 노출을 수동 검증
  - 검증 기준: `/clubs/:publicId`와 `/clubs/:publicId/meetings/new` 화면 수동 확인

- [ ] `feat: support club meeting attendance`
  - 구현: 공개 링크 참석 응답 생성/수정 흐름 연결, 기존 MeetingAttendance 규칙 재사용, 클럽원/게스트 분류 저장
  - 테스트 설명: 로그인 클럽원 자동 연결, 비로그인 이름+성별 정확 일치 시 클럽원 연결, 불일치/복수 일치 시 게스트 저장, 정원 초과 거부, CLOSED 상태 응답 거부, Competition 생성 후 응답 변경 거부 검증
  - 검증 기준: attendance service/controller 테스트와 화면 수동 확인

- [ ] `feat: enable competition from club meeting`
  - 구현: 클럽 ADMIN이 클럽 모임 참석자로 경기표 생성/삭제 가능하게 연결
  - 테스트 설명: 충분한 참석자에서 Competition 생성, 중복 생성 거부, 삭제 후 재생성 가능 검증
  - 검증 기준: club meeting competition service/controller 테스트

- [ ] `docs: align club meeting api spec`
  - 구현: 실제 Controller/DTO 필드명, 응답 shape, 권한 정책이 확정되면 이 문서와 API 설계 문서 갱신
  - 테스트 설명: 문서와 코드의 엔드포인트/필드명 수동 대조
  - 검증 기준: `git diff -- docs/features/club/club-meeting-mvp.md`

## 8. Development Validation

- [ ] 클럽 ADMIN은 클럽 모임을 생성할 수 있다.
- [ ] 일반 클럽원은 클럽 상세에서 클럽 모임 목록을 조회할 수 있다.
- [ ] 비클럽원은 클럽 상세의 클럽 모임 목록에 접근할 수 없다.
- [ ] 링크를 가진 사용자는 클럽원이 아니어도 공개 모임 화면에서 참석 응답을 남길 수 있다.
- [ ] 로그인한 활성 클럽원은 공개 모임 화면에서 클럽원 정보로 자동 입력된다.
- [ ] 비로그인 사용자는 이름과 성별이 클럽원 정보와 정확히 1명 일치할 때 클럽원으로 표시된다.
- [ ] 비로그인 사용자의 이름과 성별이 일치하지 않거나 여러 명과 일치하면 게스트로 표시된다.
- [ ] 공개 모임 화면은 클럽원 검색 결과나 클럽원 목록을 노출하지 않는다.
- [ ] 일반 클럽원은 클럽 모임 생성, 수정, 삭제, 참석자 관리, 경기표 생성을 할 수 없다.
- [ ] 클럽 ADMIN은 다른 ADMIN이 만든 클럽 모임도 관리할 수 있다.
- [ ] 기존 `/api/meetings` 독립 모임 기능은 깨지지 않는다.
- [ ] 기존 공개 참석 링크의 비로그인 참석 기능은 깨지지 않는다.
- [ ] 클럽 상세 모임 탭의 빈 상태와 목록 상태가 모두 자연스럽게 보인다.
- [ ] 정원, 상태, Competition 연결 이후 제한 규칙이 기존 Meeting과 동일하게 동작한다.
- [ ] spec, plan, code가 서로 어긋나지 않는다.

## 9. Final QA Checklist

- [ ] 클럽 생성부터 클럽 모임 생성, 참석, 경기표 생성까지 한 흐름이 완료된다.
- [ ] 참석 명단에서 클럽원과 게스트가 배지로 구분된다.
- [ ] 실패 시 사용자가 다음 행동을 알 수 있는 메시지가 나온다.
- [ ] 권한 실패는 401/403/404/409 중 의도한 상태로 전달된다.
- [ ] 클럽 삭제, 클럽원 삭제, ADMIN 강등 같은 주변 상태 변화에서도 클럽 모임 접근이 의도대로 제한된다.
- [ ] 프론트엔드와 백엔드 API 계약이 일치한다.
- [ ] 관련 단위 테스트 또는 수동 검증 결과를 PR에 설명할 수 있다.

## 10. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-07-08 | Club meeting service 책임 분리 | Controller에서 권한/clubId 조립 제거, MeetingQueryService 도메인 조회와 DTO 변환 분리 |
| 2026-07-07 | Club meeting API 구현 | `/api/clubs/{clubPublicId}/meetings` 계열 API와 clubId 기준 관리 경로 추가 |
| 2026-07-07 | Club meeting domain link 구현 | `Meeting.clubId`, club meeting create/query path, repository/service tests 추가 |
| 2026-07-07 | Club Meeting MVP 문서 추가 | Club MVP 이후 클럽 모임 연결 범위와 commit unit을 분리하기 위해 작성 |
