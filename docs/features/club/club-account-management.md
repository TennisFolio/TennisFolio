# Feature: 클럽 계정 관리

## 1. Goal

로그인 Account와 클럽 명단용 ClubMember를 분리하고, OWNER/ADMIN이 클럽 운영을 함께 수행할 수 있게 한다. OWNER는 링크로 ADMIN을 초대·관리하고, ADMIN은 본인 클럽 계정만 수정한다.

## 2. User Flow

```text
Account -> 클럽 생성 -> ClubAccount(OWNER) 생성
OWNER -> ADMIN 초대 링크 발급 -> 로그인 Account 수락 -> ClubAccount(ADMIN) 생성
OWNER -> 클럽 계정 관리 -> 계정 상세 -> 역할 변경 또는 클럽에서 탈퇴시키기
ADMIN -> 클럽 계정 -> 표시 이름 수정 또는 클럽 탈퇴
운영진 -> 클럽 모임/참석자 관리 -> Competition 공개 URL로 대진표 조회
```

## 3. Spec

- `ClubAccount(clubId, accountId, displayName, role)`만 로그인 Account와 Club을 연결한다. 역할은 `OWNER`, `ADMIN`이며 한 Account는 여러 클럽에 연결될 수 있다.
- `ClubMember`에서는 `userId`, `role`, `ClubMemberRole`을 제거한다. ClubMember와 Account를 연결하거나 MEMBER 역할을 만들지 않는다.
- OWNER/ADMIN은 회원, 클럽 모임·참석자, 대진표·결과, 통계, 클럽 정보를 공통 관리한다. OWNER만 계정 목록·초대·역할 변경·계정 제거·클럽 삭제를 수행한다.
- OWNER는 여러 명일 수 있지만 역할 변경, 계정 제거, 자진 탈퇴로 OWNER가 0명이 되는 요청은 `409`으로 거부한다.
- 초대는 클럽당 하나의 재사용 가능한 링크다. 3시간 유효, 인원 제한 없음, 새 링크 발급 시 기존 토큰 무효화, 수동 종료 가능, DB에는 토큰 해시만 저장한다.
- 초대 수락은 로그인 필수이며 전역 `User.nickName`을 `displayName` 기본값으로 제시한다. 기존 ClubAccount가 있으면 역할을 보존하고 성공 응답한다.
- 클럽 소속 Meeting과 참석자 정보는 OWNER/ADMIN만 조회·변경한다. 공개 Meeting 상세/참석 응답은 독립 Meeting에만 유지한다. Competition 공개 URL과 결과 조회는 유지한다.
- 기존 클럽, 클럽 소속 모임·참석 데이터는 배포 전 백업 뒤 초기화하고 이전하지 않는다. 독립 Meeting과 Competition은 보존한다.
- OWNER 메뉴는 `클럽 계정 관리`(`/dashboard/accounts`), ADMIN 메뉴는 `클럽 계정`(`/dashboard/account`)이다. ADMIN은 본인 수정 화면 외 계정 관리 API와 화면을 볼 수 없다.

기준 목업: `docs/features/club/club-account-management-mockup.html`

## 4. Design Review

### Responsibility / 파일 범위

- Domain·Persistence: `club/entity/ClubAccount*`, `ClubOperatorInvitation`, `ClubMember`; 각 Repository와 실제 DB 초기화 SQL.
- Authorization: `ClubAccessService`가 ClubAccount만으로 `requireOperator`, `requireOwner`, `isOperator`를 제공한다.
- API: 기존 `ClubController`에 account API를 추가하고, 초대 수락용 `ClubOperatorInvitationController`를 분리한다.
- Application: `ClubAccountCommandService`, `ClubAccountQueryService`, `ClubOperatorInvitationService`로 계정·초대 흐름을 분리한다.
- Meeting: `ClubMeeting*Service`, `Meeting*Service`, `CompetitionAdminAuthorizationService`가 ClubMember가 아닌 ClubAccount 권한을 사용한다.
- Frontend: `clubApi.js`, `ClubDashboardLayout`, 계정 목록/상세/수락 page·section 컴포넌트, `App.jsx` route를 추가한다.

### Decisions

- `ClubOperatorInvitation`은 클럽당 한 행을 갱신한다. 수락 이력 테이블이나 초대 비밀번호는 만들지 않는다.
- 계정 제거와 자진 탈퇴는 ClubAccount 행을 삭제한다. ClubMember 또는 과거 MeetingAttendance는 Account와 연결하지 않으므로 수정하지 않는다.
- 데이터 초기화 SQL은 애플리케이션 시작 시 자동 실행하지 않는다. FK 순서 검토와 백업 확인 뒤 운영자가 별도 실행한다.
- OAuth 로그인 뒤 초대 URL 복귀는 허용 목록 기반 `returnUrl`만 사용해 open redirect를 막는다.

## 5. Plan / Commit Units

- [ ] `docs: add club account management feature spec`
  - 구현: 이 문서와 목업, 기존 상세 설계의 API·권한·초기화 정책을 일치시킨다.
  - 테스트 설명: OWNER/ADMIN 화면, 마지막 OWNER, 공개 Competition 보존 요구가 모두 기록됐는지 수동 검토한다.
  - 검증 기준: `git diff --check -- docs/features/club docs/superpowers/specs/2026-09-30-club-operator-authority-design.md`

- [ ] `feat: separate club accounts from member roster`
  - 구현: `ClubAccount`, `ClubAccountRole`, `ClubAccountRepository`를 추가하고 `ClubMember`의 `userId`·`role`과 `ClubMemberRole`을 제거한다. `ClubCommandService`, `ClubQueryService`, `ClubSummaryResponse`, `ClubAccessService` 및 Club/Meeting/Competition 권한 호출부를 ClubAccount 기준으로 교체한다. 배포용 초기화 SQL과 백업·실행 순서를 문서화한다.
  - 테스트 설명: 생성자가 OWNER로 생성되고 ClubMember는 생성되지 않음, 한 Account의 다중 클럽 역할, OWNER/ADMIN 공통 운영 권한, 비운영진 거부를 service/repository 테스트로 검증한다.
  - 검증 기준: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubAccessServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubQueryServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionAdminAuthorizationServiceTest`

- [x] `feat: add owner club account management api` - done, verified
  - 구현: `ClubAccountCommandService`와 `ClubAccountQueryService`, request/response DTO를 추가한다. `ClubController`에 OWNER 목록, 대상 상세, 역할 PATCH, 대상 DELETE, 본인 GET/PATCH/DELETE를 연결하고 마지막 OWNER 변경을 `409`으로 처리한다.
  - 테스트 설명: OWNER의 목록·역할 변경·ADMIN 제거, ADMIN의 본인 이름 수정/탈퇴, 다른 Account 이름 수정 거부, ADMIN의 OWNER API `403`, 마지막 OWNER 보호 `409`를 검증한다.
  - 검증 기준: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubAccountCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubAccountQueryServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`

- [ ] `feat: add reusable admin invitation links`
  - 구현: `ClubOperatorInvitation`과 Repository·Service·Controller·DTO를 추가한다. OWNER의 발급/종료와 로그인 Account의 수락을 구현하고, `BCryptPasswordEncoder`로 토큰 해시를 저장한다. 만료·종료·교체·없는 토큰은 `404`, 비로그인 수락은 `401`로 통일하며 OAuth returnUrl 허용 검증을 추가한다.
  - 테스트 설명: 3시간 만료, 재발급 무효화, 수동 종료, 다수 수락, 같은 Account 재수락의 역할 보존, 공백 표시 이름 `400`, OWNER/ADMIN 권한 경계를 검증한다.
  - 검증 기준: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubOperatorInvitationServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubOperatorInvitationControllerTest --tests com.tennisfolio.Tennisfolio.config.ClubSecurityConfigTest`

- [ ] `fix: restrict club meetings to club accounts`
  - 구현: `ClubMeetingQueryService`, `ClubMeetingCommandService`, `MeetingQueryService`, `MeetingAttendanceCommandService`, `MeetingController`, `SecurityConfig`을 갱신한다. 클럽 모임의 상세·참석자 공개 조회/응답을 차단하고 OWNER/ADMIN만 허용하며, 독립 Meeting의 기존 공개 API와 Competition URL은 유지한다. Club 삭제 시 클럽 모임·참석자를 soft delete 처리한다.
  - 테스트 설명: OWNER/ADMIN의 클럽 모임 관리 성공, 비운영진·비로그인 클럽 모임 접근 거부, 독립 Meeting 공개 조회/참석과 Competition 공개 조회 회귀 없음을 검증한다.
  - 검증 기준: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubMeetingCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubMeetingQueryServiceTest --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryServiceTest --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandServiceTest --tests com.tennisfolio.Tennisfolio.config.MeetingSecurityConfigTest`

- [ ] `feat: add club account management screens`
  - 구현: `clubApi.js`에 계정·초대 호출을 추가하고 `ClubDashboardLayout`의 데스크톱/모바일 메뉴를 역할별로 분기한다. `ClubAccountManagementPage`, `ClubAccountDetailPage`, `ClubAccountPage`, `ClubInvitationAcceptPage`와 필요한 page-role section을 추가하고 `App.jsx`에 `/dashboard/accounts`, `/dashboard/accounts/:clubAccountId`, `/dashboard/account`, `/club-account-invitations/:token` route를 등록한다.
  - 테스트 설명: OWNER의 링크 발급/복사/종료·목록 수정 진입·상세 역할 변경/탈퇴시키기, ADMIN의 본인 이름 수정/탈퇴만 노출, 로그인 후 초대 수락 복귀와 만료 안내를 수동 검증한다.
  - 검증 기준: 브라우저에서 목업 네 화면과 동일한 메뉴·권한·오류 메시지를 확인한다. 프런트엔드 테스트와 npm build는 별도 허가가 있을 때만 실행한다.

- [ ] `docs: record club account verification`
  - 구현: 실제 API 응답, 초기화 실행 결과, 테스트·수동 검증 결과를 이 문서의 Development Validation과 Change Log에 기록하고 상세 설계와 차이가 있으면 함께 갱신한다.
  - 테스트 설명: 생성→초대→수락→역할 변경/탈퇴→클럽 모임 관리→Competition 공개 조회 전체 흐름을 점검한다.
  - 검증 기준: `git diff --check -- docs/features/club/club-account-management.md docs/superpowers/specs/2026-09-30-club-operator-authority-design.md`

## 6. Development Validation

- [ ] Account와 ClubMember가 DB/API/권한 코드에서 분리됐다.
- [ ] OWNER/ADMIN/비운영진의 성공·403·409 경계가 테스트됐다.
- [ ] 초대 토큰 원문이 DB·로그·응답 이력에 저장되지 않는다.
- [ ] 클럽 모임은 운영진만 접근하고 독립 모임·Competition 공개 URL은 유지된다.
- [ ] 초기화 SQL의 대상과 백업 절차를 검토했다.

## 7. Final QA Checklist

- [ ] 새 클럽 생성자가 OWNER가 되고 여러 클럽을 전환할 수 있다.
- [ ] OWNER가 ADMIN을 초대·역할 변경·탈퇴시킬 수 있고 마지막 OWNER는 보호된다.
- [ ] ADMIN은 본인 클럽 계정만 수정·탈퇴할 수 있다.
- [ ] 클럽 모임/참석자 정보는 외부에 노출되지 않고 대진표 공개 URL은 계속 열린다.

## 8. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-09-30 | 초기 spec 및 commit 단위 계획 작성 | ClubAccount 권한 분리와 ADMIN 초대 구현 범위 확정 |
