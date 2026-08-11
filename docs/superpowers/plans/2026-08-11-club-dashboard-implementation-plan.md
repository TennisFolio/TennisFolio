# 클럽 대시보드 구현 계획

> 작업 추적은 체크박스로 한다. 구현 전에는 `docs/superpowers/specs/2026-08-11-club-backoffice-dashboard-design.md`와 `docs/features/club/club-backoffice-dashboard-mockup.html`을 함께 확인한다.

**목표:** 클럽 ADMIN이 최근 30일의 멤버 구성·모임 활동·참여 요약을 한 화면에서 조회하게 한다.

**구조:** 프런트는 `/clubs/:clubPublicId/dashboard`에서 대시보드 API를 한 번 호출한다. 백엔드는 `ClubAccessService`로 ADMIN 권한을 확인한 뒤, 대시보드 전용 조회 저장소의 목적별 집계를 `ClubDashboardQueryService`에서 하나의 응답으로 조립한다.

**기술:** Spring Boot, JPA/Querydsl, React, React Router, 기존 `apiRequestSilent`와 Club CSS.

---

## 파일 구조

| 파일 | 역할 |
|---|---|
| `club/repository/ClubDashboardQueryRepository.java` | 대시보드에 필요한 DB 집계와 최근 모임 조회를 전담한다. |
| `club/service/ClubDashboardQueryService.java` | 권한 확인, 최근 30일 계산, 응답 조립을 담당한다. |
| `club/dto/ClubDashboardResponse.java` | API 응답과 중첩 요약 DTO를 정의한다. |
| `club/api/ClubController.java` | `GET /api/clubs/{clubPublicId}/dashboard`를 노출한다. |
| `club/.../ClubDashboardQueryRepositoryTest.java` | 삭제·취소·참석 분류를 포함한 DB 집계를 검증한다. |
| `club/.../ClubDashboardQueryServiceTest.java` | 30일 경계, ADMIN 권한, 0건 응답 조립을 검증한다. |
| `club/.../ClubControllerTest.java` | 경로, 인증 principal, 응답 계약을 검증한다. |
| `src/tennisFolio/src/utils/clubApi.js` | 대시보드 조회 함수 하나를 추가한다. |
| `src/tennisFolio/src/App.jsx` | 대시보드 경로를 등록한다. |
| `src/tennisFolio/src/page/club/ClubDashboardPage.jsx` | 데이터 로딩, 권한 오류/빈 상태, 클럽 전환과 화면 조립을 담당한다. |
| `src/tennisFolio/src/components/club/dashboard/*` | 요약 수치, 구성 차트, 최근 활동, 참여 요약을 섹션 단위로 렌더링한다. |
| `src/tennisFolio/src/page/Club.css` | 데스크톱 대시보드 레이아웃과 상태 스타일을 추가한다. |

## 1. 대시보드 집계 조회와 응답 계약

- [ ] `ClubDashboardQueryRepository`와 `ClubDashboardResponse`를 만든다.
  - 활성 멤버 수·성별 분포·클럽 설정 실력 등급별 인원·미분류 인원을 반환한다.
  - 기간 내 취소 제외 모임 수, 취소 모임 수, 멤버/게스트 ATTENDING 건수, 고유 멤버 참석자 수, 최근 모임 3건을 각각 DB에서 집계한다.
  - `deletedAt`이 있는 모임/참석은 전부 제외하고, 멤버 참석은 `CLUB_MEMBER`와 `clubMemberId`가 있는 항목으로 한정한다.
- [ ] `ClubDashboardQueryRepositoryTest`를 작성한다.
  - 활성·비활성 멤버, 삭제 참석, 취소 모임, 멤버/게스트 ATTENDING·WAITING·NOT_ATTENDING을 섞어 저장한다.
  - 취소 제외 모임 수, 취소 수, 고유 멤버 참여 수, 게스트 건수, 등급 순서와 미분류 수가 spec의 규칙과 일치함을 검증한다.
- [ ] 테스트를 실행한다.
  - 명령: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepositoryTest`
  - 기대: 대시보드 집계 규칙을 검증하는 테스트 통과.

## 2. ADMIN 전용 대시보드 서비스와 API

- [ ] `ClubDashboardQueryService.getDashboard(String clubPublicId, Long currentUserId)`를 추가한다.
  - `ClubAccessService.requireAdmin`으로 클럽과 권한을 확보한다.
  - 한국 표준시 기준 오늘을 포함한 30일의 시작·종료 시각을 계산한다.
  - 저장소 결과로 `meetingCount`, `cancelledMeetingCount`, `participantCount`, 정수 반올림 참여율, 미참여 활성 멤버 수, 소수 첫째 자리 평균 참석을 조립한다.
  - 모임이 없거나 활성 멤버가 없을 때는 모든 계산값을 0으로 반환한다.
- [ ] `ClubDashboardQueryServiceTest`를 작성한다.
  - ADMIN의 정상 집계, 일반 멤버의 403, 비로그인 401, 30일 경계 밖 모임 제외, 데이터 없음의 0값 응답을 검증한다.
- [ ] `ClubController`에 `GET /api/clubs/{clubPublicId}/dashboard`를 추가하고 인증 사용자 ID를 서비스에 전달한다.
- [ ] `ClubControllerTest`에 정상 JSON 계약, `401`, `403`, `404`를 추가한다.
- [ ] 관련 백엔드 테스트를 실행한다.
  - 명령: `.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepositoryTest --tests com.tennisfolio.Tennisfolio.club.service.ClubDashboardQueryServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`
  - 기대: 새 집계·권한·HTTP 계약 테스트 통과.

## 3. 대시보드 프런트 화면과 단일 API 연동

- [ ] `clubApi.js`에 `getClubDashboard(publicId)`를 추가하고 `/api/clubs/{publicId}/dashboard`만 호출하도록 한다.
- [ ] `App.jsx`에 `/clubs/:clubPublicId/dashboard` 경로를 등록한다.
- [ ] `ClubDashboardPage`를 만든다.
  - 로그인하지 않은 사용자는 기존 `ClubAuthRequired`를 사용한다.
  - 페이지 진입 시 `getMyClubs`와 선택한 클럽의 `getClubDashboard`를 로드한다.
  - 클럽 선택 변경 시 URL을 바꾸고 해당 클럽 대시보드를 다시 조회한다.
  - 401/403/404 및 빈 데이터는 기존 Club 페이지의 메시지·빈 상태 패턴으로 처리한다.
- [ ] `components/club/dashboard`에 다음 섹션 컴포넌트를 추가한다.
  - `DashboardKpiCards`: 활성 멤버, 모임 수, 멤버 참여율, 모임당 평균 참석.
  - `MemberCompositionPanel`: 성별 막대와 `skillTierCounts`의 `level` 순서 막대, 미분류 막대.
  - `RecentActivityPanel`: 최근 모임 최대 3건의 날짜·제목·상태·멤버/게스트 참석.
  - `ParticipationSummaryPanel`: 멤버 참석 건수, 게스트 참석 건수, 미참여 활성 멤버.
- [ ] `Club.css`에 목업과 같은 좌측 메뉴·상단 클럽 선택·카드·막대그래프·빈 상태를 데스크톱 우선으로 추가한다.
  - 1차에서는 대시보드 외 메뉴를 비활성 텍스트로 표시하며 링크/상세 화면을 만들지 않는다.
- [ ] 프런트 수동 검증을 수행한다.
  - ADMIN으로 대시보드 진입, 운영 클럽 전환, 정상/데이터 없음/권한 없음 상태를 브라우저에서 확인한다.
  - 프런트 테스트와 npm 빌드는 프로젝트 규칙에 따라 이 계획의 기본 검증에서 제외한다.

## 4. 문서 정합성과 완료 확인

- [ ] 실제 API DTO 이름, 응답 필드, 집계 규칙을 spec에 반영한다.
- [ ] 목업과 실제 화면에서 게스트 참석은 참여 요약에 한 번만 보이고, 상단 네 번째 수치는 모임당 평균 참석인지 확인한다.
- [ ] `Verification Log`에 실행한 백엔드 테스트와 수동 화면 검증 결과를 기록한다.
- [ ] 제안 커밋 단위는 아래와 같다. 실제 커밋 전에는 staged 파일, 검증 결과, 커밋 메시지를 사용자에게 보여주고 확인을 받는다.
  - `feat: add club dashboard aggregation API`
  - `feat: add club dashboard screen`
  - `docs: align club dashboard specification`

## 자체 검토

- 권한·30일 집계·삭제/취소 제외·멤버/게스트 분리·클럽 설정 등급·빈/오류 상태는 1~3단계에서 모두 다룬다.
- 게임·대진·승패, 기간 변경, 상세 메뉴는 계획에 포함하지 않는다.
- 프런트 테스트와 빌드는 명시적으로 제외했으며, 백엔드 검증 명령만 계획에 기록했다.
