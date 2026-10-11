# 클럽 회원 관리 구현 계획

> 작업 전 [회원 관리 설계](../specs/2026-09-23-club-member-management-design.md)와 세 HTML 목업을 확인한다.

**목표:** 대시보드에서 회원 생애주기와 복사·붙여넣기 기반 등록을 관리한다.

**구조:** 백엔드는 `memberStatus`를 활성 여부의 단일 기준으로 이관하고, 관리자 조회·상태 변경 API를 제공한다. 프런트엔드는 대시보드 공용 chrome 위에 목록·상세·붙여넣기 페이지를 구성한다.

**기술:** Spring Boot, JPA, React, React Router, 기존 `apiRequestSilent`, `Club.css` 및 competition theme variables.

## 파일 구조

| 파일 | 작업 |
|---|---|
| `club/entity/ClubMember.java`, `ClubMemberStatus.java` | 상태·날짜 필드와 상태 전환 모델 |
| `club/repository/ClubMemberRepository.java` | 상태·모든 상태 이름 조회 |
| `club/service/ClubMemberCommandService.java`, `ClubQueryService.java`, `ClubAccessService.java` | 등록, 조회, 권한, 상태 전환 |
| `club/dto/*Member*.java`, `club/api/ClubController.java` | 관리 API와 ISO 날짜 계약 |
| `src/main/resources/query/club-member-status.sql` | 데이터 이관 스크립트 |
| `src/tennisFolio/src/page/club/*`, `components/club/*`, `utils/clubApi.js`, `clubPageUtils.js`, `Club.css`, `App.jsx` | 대시보드 회원 관리 UI·라우팅 |
| `ClubMemberCommandServiceTest.java`, `ClubControllerTest.java`, `ClubDashboardQueryRepositoryTest.java` | 상태 규칙, API 계약, 활동 회원 집계 회귀 검증 |

## 1. 상태 모델과 데이터 이관

- [ ] `ClubMemberStatus`를 만들고 `ClubMember`에 상태·가입일·휴면일·탈퇴일을 추가한다.
- [ ] `club-member-status.sql`에 열 추가, 기존 `ACTIVE=true → ACTIVE`, `ACTIVE=false → WITHDRAWN`, `ACTIVE` 열 제거 순서의 수동 이관 SQL을 작성한다.
- [x] 출생연도 입력·응답·엔티티를 제거하고 `club-member-drop-birth-year.sql`에 `BIRTH_YEAR` 삭제 DDL을 작성한다.
- [ ] `ClubMemberRepository`, `ClubAccessService`, dashboard/meeting의 활성 회원 조회를 `memberStatus=ACTIVE`로 교체한다.
- [ ] `ClubMemberCommandServiceTest`에 휴면·탈퇴·복구 날짜 및 마지막 활동 관리자 차단을 작성한다.
- [ ] 검증: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.repository.ClubDashboardQueryRepositoryTest`

## 2. 관리 API와 원자적 등록

- [x] 생성·수정 response/request DTO에 `LocalDate` 가입일·상태 날짜를 추가하고, 새 회원을 서버에서 `ACTIVE`로 만든다.
- [x] `GET /members/management`, `GET /members/management/{memberId}`를 `ClubController`와 query service에 추가한다.
- [x] `PATCH /members/{memberId}/status`를 `ClubController`와 command service에 추가한다. (`7f96512`)
- [x] 모든 상태에서 동명이인을 막고, 탈퇴 회원은 상태 변경으로만 복구하도록 service와 repository를 변경한다.
- [x] `ClubControllerTest`에 API 경로와 principal 전달을, command service test에 일괄 요청 원자성·동명이인 규칙을 추가한다.
- [ ] 검증: `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest --tests com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandServiceTest`

## 3. 대시보드 목록과 상세

- [x] `ClubDashboardPage`의 사이드바·상단 클럽 전환기를 공용 컴포넌트로 추출하고 회원 관리 메뉴를 연결한다.
- [x] `ClubMemberManagementPage`와 목록/필터/상태 요약 섹션을 추가하고 `/dashboard/members` 경로를 등록한다.
- [x] `ClubMemberDetailPage`와 기본 정보·상태 편집 섹션을 추가하고 `/dashboard/members/:memberId` 경로를 등록한다.
- [x] 기존 클럽 상세에서 추가·일괄 추가·인라인 수정 진입점을 제거하고 회원 관리로 이동하게 한다. 이전 독립 회원 생성·수정 화면/라우트는 새 경로로 대체한다.
- [ ] 프런트엔드 테스트·빌드는 프로젝트 규칙상 실행하지 않는다. 관리자/일반 회원 접근, 필터·상세 저장·목록 복귀를 브라우저에서 수동 확인한다.

## 4. 엑셀 붙여넣기 등록과 실패 표시

- [x] `bulkMemberUtils.js`와 bulk view/page를 5개 열 TSV, ISO 날짜 검증, 행별 오류 미리보기로 변경한다.
- [x] `/dashboard/members/paste` 경로에서 붙여넣기 입력과 미리보기·등록을 구현한다.
- [x] 409 시 최신 관리 목록을 다시 받아 충돌 행을 표시하며 입력과 미리보기를 보존한다. 400/401/403/5xx는 각각 적절한 오류 메시지를 표시하고 부분 등록하지 않는다.
- [ ] 프런트엔드 테스트·빌드는 실행하지 않는다. 정상 등록, 날짜 오류, 입력 중복, 동시 등록 409, 권한 오류를 브라우저에서 수동 확인한다.

## 논리적 커밋 단위

- `feat: add club member lifecycle and management API`
- `feat: add dashboard member management screens`
- `feat: add paste-based member registration`

커밋 전에는 staged 파일 목록, 검증 결과, 커밋 메시지를 사용자에게 제시하고 확인을 받는다.
