# Feature: 공개 모임 열람과 모임장 전용 운영

## 1. Goal

모든 사용자는 로그인 없이 모임 목록과 상세를 열람할 수 있고, 로그인한 모임장만 모임 생성과 운영을 할 수 있게 한다. 공개 화면에서는 날짜와 시간을 우선적으로 보여 주고, 게스트 참석 체크와 참석 마감 기능은 제공하지 않는다. 모임장은 게스트용 공개 링크를 복사할 수 있다.

## 2. User Flow

```text
게스트 -> 모임 목록 조회 -> 모임 선택 -> 날짜·시간·안내·참가자 명단·경기표 조회
로그인 사용자 -> 모임 생성 -> 본인 모임 상세 -> 모임장 관리 화면 -> 참가자와 경기표 운영
게스트 -> /api/auth/me 401 -> 토큰 재발급 없이 공개 모임 화면 유지
만료된 로그인 사용자 -> 보호 API 401 -> refresh/session 쿠키로 재발급 -> 원래 요청 재시도
쿠키 없는 재발급 요청 -> 401 응답 -> JWT 파싱 및 ERROR 로그 없음
```

## 3. Spec

- `GET /api/meetings`와 `GET /api/meetings/{publicId}`는 비로그인 사용자도 조회할 수 있다. 목록에는 일반 모임과 클럽 모임을 모두 포함한다.
- 공개 상세는 제목, 날짜·시간, 안내, 참가자 명단, 연결된 경기표를 읽기 전용으로 제공한다.
- 모임 생성, 수정, 삭제, 참가자 추가·수정·삭제, 경기표 생성·삭제는 로그인한 모임장만 할 수 있다.
- 게스트 참석 체크와 공개 `attendances` 생성·수정 API는 제거한다. 모임장은 기존 관리자용 `participants` API로 참가자를 관리한다.
- 참석 마감·재개 UI와 API는 제거한다. 기존 저장된 상태 값은 호환성을 위해 유지하되, 새 공개 동작의 권한 기준으로 사용하지 않는다.
- 모임장 관리 화면에는 게스트용 공개 URL인 `/meetings/{meetingPublicId}`만 복사하는 공유 링크 버튼을 제공한다. 클럽 운영 URL(`/clubs/{clubPublicId}/meetings/{meetingPublicId}`)은 복사하지 않는다.
- 공개 화면과 모임 목록에는 공유 링크 복사 버튼을 제공하지 않는다.
- `/clubs/{clubPublicId}/meetings/{meetingPublicId}`는 기존에 공유됐을 수 있는 클럽 운영 URL이므로, 누구나 게스트 공개 URL(`/meetings/{meetingPublicId}`)로 자동 이동한다. 실제 클럽 운영 화면은 `/clubs/{clubPublicId}/meetings/{meetingPublicId}/manage`로 분리한다.
- 클럽 관리 URL의 모임 조회가 401 또는 403이면 오류 화면을 보이지 않고 게스트 공개 URL(`/meetings/{meetingPublicId}`)로 대체 이동한다. 그 밖의 조회 오류는 관리 화면 오류로 표시한다.
- 공개 목록과 상세의 날짜·시간은 제목·보조 정보보다 먼저 읽히도록 시각적 위계를 높인다. Competition UI가 아닌 Meeting UI이므로 기존 Meeting 스타일을 사용한다.
- `/api/auth/me` 401은 토큰 재발급 대상에서 제외한다. 다른 보호 API의 401 재발급과 재시도는 유지한다.
- `refresh_token` 또는 `session_id`가 없거나 공백인 `/api/auth/reissue` 요청은 `ReIssueService` 호출 전 401로 끝낸다.

## 4. Design Review

### Responsibility

- `MeetingController`, `MeetingQueryService`, `SecurityConfig`: 공개 목록·상세 조회와 모임장 전용 변경 권한을 분리한다.
- `MeetingPublic`, `Meetings`와 하위 컴포넌트: 공개 읽기 전용 목록·상세를 구성하고, 참석 체크와 공유 UI를 제거한다.
- `MeetingManage`: 모임장 전용 참가자 운영과 경기표 기능을 유지하고, 권한 없는 클럽 관리 URL 접근은 게스트 공개 URL로 대체 이동하며, 게스트 공개 URL 복사를 제공한다.
- `App`과 클럽 모임 이동 링크: 기존 클럽 상세 URL을 공개 URL로 리디렉션하고, 모임장 내부 이동은 새 `/manage` URL만 사용한다.
- `apiClient.js`, `AuthController`: 비로그인 조회 중 발생하는 빈 토큰 재발급을 차단한다.
- 서버 테스트: 공개 조회, 모임장 전용 변경, 빈 재발급 쿠키를 회귀 검증한다.

### Review

- 공개 열람과 운영 권한은 API 경계에서 분리한다. UI를 숨기는 것만으로 권한을 처리하지 않는다.
- 기존 `OPEN`, `CLOSED`, `CANCELLED` 저장 데이터는 삭제하거나 마이그레이션하지 않는다.
- 공개 명단 노출은 제품 요구사항이며, 개인 연락처·계정 정보는 응답에 추가하지 않는다.

## 5. Plan / Commit Units

- [x] `fix(auth): prevent anonymous token reissue` - implemented, test execution skipped
  - 구현: `/api/auth/me` 401을 Axios 재발급 제외 대상에 추가하고, 빈 refresh/session 쿠키의 재발급 요청을 401로 반환한다.
  - 테스트: `AuthControllerTest`에 빈 쿠키 재발급 401과 서비스 미호출을 추가한다. 프론트 정적 테스트로 `/api/auth/me` 제외 조건을 확인한다.
  - 검증: 허가 시 `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest`를 실행한다.

- [x] `feat(meeting): expose read-only meeting discovery` - implemented, test execution skipped
  - 구현: 공개 모임 목록 조회 API와 보안 규칙을 추가하고, 클럽 모임도 비로그인 상세 조회를 허용한다. 생성·운영·참가자 변경 API는 인증 및 owner 권한을 유지한다.
  - 테스트: `MeetingQueryServiceTest`, `MeetingControllerTest`에 공개 목록·상세 조회 계약을 추가한다. 보안 규칙은 정적 검토로 확인한다.
  - 검증: 허가 시 관련 Gradle 테스트를 실행한다.

- [ ] `refactor(meeting): remove public attendance controls`
  - 구현: 공개 상세를 읽기 전용으로 단순화하고, 게스트 참석 체크와 참석 마감/재개를 제거한다. 공개 화면은 이름·성별 입력, 참가 등록, 기존 응답 선택 없이 모임 정보와 명단을 바로 표시한다. 공개 화면·목록의 공유 링크 복사는 제거하고, 모임장 관리 화면에는 `/meetings/{meetingPublicId}`를 복사하는 버튼만 유지한다.
  - 테스트: 프론트엔드 테스트는 프로젝트 규칙에 따라 작성·실행하지 않는다. 정적 검색으로 제거된 참조가 남지 않는지 확인한다.
  - 검증: 비로그인 공개 목록·상세와 로그인 모임장 관리 화면을 수동으로 확인한다.

- [ ] `feat(meeting): prioritize schedule information`
  - 구현: 모임 목록과 공개 상세의 날짜·시간 블록을 상단 주요 정보로 재배치하고 기존 Meeting CSS로 가독성을 높인다.
  - 테스트: 프론트엔드 테스트는 작성·실행하지 않는다.
  - 검증: 데스크톱과 모바일 화면에서 날짜·시간이 제목과 참가자 정보보다 먼저 읽히는지 수동 확인한다.

- [x] `fix(meeting): separate public and club management URLs` - implemented, frontend test execution skipped
  - 구현: 기존 클럽 모임 상세 URL을 공개 상세로 리디렉션하고, 관리 화면 라우트를 `/manage`로 분리한다. 클럽 생성·수정·관리 화면의 이동 링크를 새 관리 URL로 변경한다. `/manage`의 모임 조회가 401·403이면 공개 상세로 대체 이동한다.
  - 테스트: 프론트엔드 테스트는 프로젝트 규칙에 따라 작성·실행하지 않는다.
  - 검증: 게스트가 기존 클럽 URL을 열면 공개 상세로 이동하고, 로그인한 운영자는 새 관리 URL에서 기존 운영 기능을 사용할 수 있는지 수동 확인한다.

## 6. Development Validation

- [ ] 게스트는 로그인 없이 모든 모임 목록과 상세를 조회할 수 있다.
- [ ] 게스트는 참가자 명단과 경기표를 읽을 수 있지만 참가자·모임을 변경할 수 없다.
- [ ] 로그인한 모임장만 생성과 운영을 수행할 수 있다.
- [ ] 게스트 참석 체크, 참석 마감/재개, 공개 화면·목록의 공유 링크 복사 UI와 공개 변경 API가 없다.
- [ ] 모임장 관리 화면의 공유 버튼은 게스트용 `/meetings/{meetingPublicId}`만 복사한다.
- [ ] 기존 클럽 운영 URL 또는 권한 없는 `/manage` 접근은 공개 URL로 이동하고, 운영 화면은 권한 있는 사용자에게만 연다.
- [ ] 비로그인 `/api/auth/me` 401은 재발급 요청을 만들지 않는다.
- [ ] 쿠키 없는 `/api/auth/reissue`는 JWT 예외 로그 없이 401이다.

## 7. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-10-12 | 초기 작성 | 공개 모임 열람과 로그인 기반 모임 운영 요구사항 정리 |
| 2026-10-12 | 모임장 공개 링크 복사 추가 | 클럽 운영 URL 대신 게스트용 공개 URL을 전달 |
| 2026-10-12 | 클럽 관리 URL 분리 추가 | 잘못 공유된 운영 URL도 게스트 공개 화면으로 연결 |
| 2026-10-12 | 권한 없는 관리 URL 대체 이동 추가 | 게스트가 `/manage` 오류 화면에 머물지 않도록 공개 화면으로 연결 |
| 2026-10-12 | 공개 참가 등록 UI 제거 | 게스트는 모임 정보와 명단만 읽고, 참가자는 모임장이 관리 |
| 2026-10-12 | 공개 모임 목록 추가 | 비로그인 사용자도 모든 활성 모임을 목록에서 열람 |
| 2026-10-12 | 비로그인 토큰 재발급 방어 구현 | `/api/auth/me` 재발급 제외 및 빈 재발급 쿠키 401 처리 |
