# 클럽별 실력 등급 관리 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 클럽 관리자가 등급을 구성하고 클럽원에게 등급 또는 미정을 배정할 수 있게 한다.

**Architecture:** `ClubSkillTier`를 클럽 소유 엔티티로 두고, `ClubMember`는 nullable 연관관계로 선택된 등급을 참조한다. 등급 편집은 클럽 생성·수정 요청에 포함하며, 순서 변경 때마다 서버가 `level`을 N~1로 재계산한다.

**Tech Stack:** Spring Boot, Spring Data JPA, MySQL/Hibernate, React, Vite

---

## API 계약

모든 성공 응답은 기존 `ResponseDTO` 래퍼를 유지한다. 아래 예시의 `data`만 API별로 달라지며, 공통 형식은 `{ "code": "SUCCESS", "message": "...", "data": ... }`이다. 등급 변경을 위해 별도 API를 만들지 않고, 클럽 생성·수정 요청 안에서 등급 목록 전체를 교체한다.

### 1. 클럽 생성 — `POST /api/clubs`

**요청** — `skillTiers`는 생략하거나 빈 배열로 보낼 수 있다. 새 등급의 `id`는 보내지 않는다.

```json
{
  "name": "테니스폴리오",
  "description": "주말 복식 클럽",
  "skillTiers": [
    { "name": "A" },
    { "name": "B" },
    { "name": "C" }
  ]
}
```

**응답 `200 OK`** — 기존 호환성을 위해 `publicId`만 반환한다.

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { "publicId": "c931eb52-d569-490e-8a03-3fddcaef2974" }
}
```

### 2. 클럽 상세 조회 — `GET /api/clubs/{clubPublicId}`

**요청** — 경로 변수 `clubPublicId`만 사용한다.

**응답 `200 OK`** — `skillTiers`는 강한 순서(`level` 내림차순)로 반환한다. 내부 계산값인 `level`은 UI에 노출하지 않지만, API에는 포함해 정렬 기준을 명확히 한다.

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "publicId": "c931eb52-d569-490e-8a03-3fddcaef2974",
    "name": "테니스폴리오",
    "description": "주말 복식 클럽",
    "currentUserRole": "ADMIN",
    "admin": true,
    "memberCount": 12,
    "skillTiers": [
      { "id": 101, "name": "A", "level": 3 },
      { "id": 102, "name": "B", "level": 2 },
      { "id": 103, "name": "C", "level": 1 }
    ]
  }
}
```

### 3. 클럽 수정 — `PATCH /api/clubs/{clubPublicId}`

**요청** — 기존 등급은 `id`를 포함하고, 새 등급은 `id` 없이 보낸다. 배열 순서가 강한 순서이며, 서버가 `level = 배열 길이 - 인덱스`로 다시 계산한다. 목록에서 빠진 기존 등급은 삭제되고, 해당 등급을 가진 모든 클럽원은 `미정`(`skillTierId: null`)이 된다.

```json
{
  "name": "테니스폴리오",
  "description": "주말 복식 클럽",
  "skillTiers": [
    { "id": 101, "name": "상급" },
    { "name": "중급" },
    { "id": 103, "name": "초급" }
  ]
}
```

**응답 `200 OK`** — 현재 API와 동일하게 본문 데이터는 없다.

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

### 4. 클럽원 목록 조회 — `GET /api/clubs/{clubPublicId}/members`

**요청** — 기존 쿼리 파라미터를 유지한다. 등급 관련 파라미터는 추가하지 않는다.

**응답 `200 OK`** — 기존 `skillNote`를 제거하고, 선택하지 않은 클럽원은 등급 필드가 모두 `null`이다.

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": [
    {
      "id": 42,
      "userId": null,
      "name": "김테니스",
      "gender": "MALE",
      "role": "MEMBER",
      "skillTierId": 101,
      "skillTierName": "상급",
      "skillTierLevel": 3,
      "contactMemo": "010-1234-5678",
      "memo": ""
    }
  ]
}
```

### 5. 클럽원 등록 — `POST /api/clubs/{clubPublicId}/members`

**요청** — `skillTierId`가 `null`이거나 생략되면 `미정`으로 등록한다. ID를 보낼 때는 반드시 해당 클럽의 등급이어야 한다.

```json
{
  "name": "김테니스",
  "gender": "MALE",
  "role": "MEMBER",
  "skillTierId": 101,
  "contactMemo": "010-1234-5678",
  "memo": "첫 방문"
}
```

**응답 `200 OK`**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

### 6. 클럽원 수정 — `PATCH /api/clubs/{clubPublicId}/members/{memberId}`

**요청** — 등록 요청과 같은 필드를 사용한다. `skillTierId: null`로 보내면 기존 등급을 해제해 `미정`으로 변경한다.

```json
{
  "name": "김테니스",
  "gender": "MALE",
  "role": "MEMBER",
  "skillTierId": null,
  "contactMemo": "010-1234-5678",
  "memo": "등급 확인 필요"
}
```

**응답 `200 OK`**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

### 공통 검증 및 오류 응답

- `skillTiers`는 0~10개이며, 각 `name`은 공백 제거 뒤 비어 있으면 안 되고 최대 10자이다. 같은 클럽 안에서 이름은 중복될 수 없다.
- 수정 요청의 등급 `id`는 반드시 대상 클럽의 기존 등급이어야 한다.
- `skillTierId`는 대상 클럽의 등급만 허용한다. 다른 클럽의 ID 또는 존재하지 않는 ID는 `400 Bad Request`로 거절한다.
- 위 검증 실패는 기존 오류 응답 형식(`code`, `message`, `data`)을 유지하며, `data`는 `null`이다.

## 파일 구조

| 파일 | 책임 |
|---|---|
| `club/entity/ClubSkillTier.java` | 클럽 등급의 이름과 계산용 `level`을 보관한다. |
| `club/repository/ClubSkillTierRepository.java` | 클럽별 등급 조회와 삭제 대상 조회를 담당한다. |
| `club/service/ClubSkillTierService.java` | 등급 목록 검증, N~1 재번호 부여, 삭제된 등급의 클럽원 미정 전환을 담당한다. |
| `club/dto/ClubSkillTierRequest.java`, `ClubSkillTierResponse.java` | 클럽 등급 API 계약을 표현한다. |
| 기존 Club/ClubMember DTO·서비스·응답 | 등급 목록과 nullable `skillTierId`를 API 계약에 연결하고 `skillNote`를 제거한다. |
| `ClubCreateView`, `ClubEditView`, `ClubMemberForm` | 클럽 등급 편집과 클럽원 등급 선택 UI를 제공한다. |

## Task 1: 배포용 데이터베이스 스키마 변경 준비

**Files:**
- Create: `src/main/resources/query/club-skill-tier.sql`

- [ ] **Step 1: 적용 전 데이터 상태를 확인한다.**
  - 실행할 SQL: `SELECT COUNT(*) AS skill_note_count FROM tb_club_member WHERE SKILL_NOTE IS NOT NULL AND TRIM(SKILL_NOTE) <> '';`
  - 기대 결과: 삭제될 `skillNote` 데이터 수를 배포 전에 확인한다.

- [ ] **Step 2: 명시적 DDL 스크립트를 작성한다.**
  - `tb_club_skill_tier` 테이블에 `CLUB_SKILL_TIER_ID`, `CLUB_ID`, `NAME`, `LEVEL`, `CRT_DT`, `UPD_DT`를 생성한다.
  - `CLUB_ID` 외래 키와 `(CLUB_ID, NAME)` 유니크 제약, `(CLUB_ID, LEVEL)` 유니크 제약을 추가한다.
  - `tb_club_member`에 nullable `SKILL_TIER_ID` 외래 키를 추가한 뒤 `SKILL_NOTE` 컬럼을 삭제한다.
  - MySQL 스크립트는 다음 순서를 유지한다.

```sql
CREATE TABLE tb_club_skill_tier (
  CLUB_SKILL_TIER_ID BIGINT NOT NULL AUTO_INCREMENT,
  CLUB_ID BIGINT NOT NULL,
  NAME VARCHAR(10) NOT NULL,
  LEVEL INT NOT NULL,
  CRT_DT DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  UPD_DT DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (CLUB_SKILL_TIER_ID),
  CONSTRAINT uk_club_skill_tier_name UNIQUE (CLUB_ID, NAME),
  CONSTRAINT uk_club_skill_tier_level UNIQUE (CLUB_ID, LEVEL),
  CONSTRAINT fk_club_skill_tier_club FOREIGN KEY (CLUB_ID) REFERENCES tb_club (CLUB_ID)
);
ALTER TABLE tb_club_member ADD COLUMN SKILL_TIER_ID BIGINT NULL;
ALTER TABLE tb_club_member ADD CONSTRAINT fk_club_member_skill_tier
  FOREIGN KEY (SKILL_TIER_ID) REFERENCES tb_club_skill_tier (CLUB_SKILL_TIER_ID);
ALTER TABLE tb_club_member DROP COLUMN SKILL_NOTE;
```

- [ ] **Step 3: 스테이징 배포 전에 DDL을 적용한다.**
  - `application-stg.properties`에는 자동 스키마 갱신 설정이 없으므로, 애플리케이션 배포보다 먼저 표준 DB 배포 절차로 위 스크립트를 실행한다.
  - 기대 결과: 새 애플리케이션 버전이 실행되기 전에 테이블·외래 키·컬럼 구성이 준비된다.

## Task 2: 등급 도메인과 클럽원 참조 추가

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/club/entity/ClubSkillTier.java`
- Create: `src/main/java/com/tennisfolio/Tennisfolio/club/repository/ClubSkillTierRepository.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/entity/ClubMember.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/repository/ClubMemberRepository.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/club/repository/ClubSkillTierRepositoryTest.java`

- [ ] **Step 1: 실패하는 영속성 테스트를 작성한다.**
  - 한 클럽의 등급을 `level` 내림차순으로 조회하는 경우와, 다른 클럽의 등급이 섞이지 않는 경우를 검증한다.
  - `ClubMember.skillTier`가 nullable로 저장되는 경우를 검증한다.

- [ ] **Step 2: 테스트가 실패하는지 확인한다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepositoryTest`
  - 기대 결과: `ClubSkillTier` 또는 repository가 없어 컴파일/테스트 실패.

- [ ] **Step 3: 최소 도메인 모델을 구현한다.**
  - `ClubSkillTier`에 `CLUB_SKILL_TIER_ID`, `CLUB_ID`, `NAME`, `LEVEL` 컬럼을 둔다.
  - `ClubMember.SKILL_NOTE`와 관련 생성자·수정 인자를 제거하고 nullable `@ManyToOne skillTier`를 추가한다.
  - repository에 `findByClubOrderByLevelDescIdAsc(Club club)`와 삭제될 등급을 참조한 회원 조회 메서드를 추가한다.

- [ ] **Step 4: 영속성 테스트를 통과시킨다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepositoryTest`
  - 기대 결과: PASS.

## Task 3: 등급 목록 저장과 클럽 API 계약 구현

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubSkillTierRequest.java`
- Create: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubSkillTierResponse.java`
- Create: `src/main/java/com/tennisfolio/Tennisfolio/club/service/ClubSkillTierService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubCreateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubUpdateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubDetailResponse.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/service/ClubCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/service/ClubQueryService.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/club/service/ClubCommandServiceTest.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/club/api/ClubControllerTest.java`

- [ ] **Step 1: 실패하는 서비스와 API 계약 테스트를 작성한다.**
  - 클럽을 등급 없이 생성할 수 있는지 검증한다.
  - 4개 등급을 저장하면 관리자가 보낸 순서대로 `4, 3, 2, 1`이 부여되는지 검증한다.
  - 11개 등급, 빈 이름, 10자 초과 이름, 같은 클럽의 중복 이름은 400으로 거부되는지 검증한다.
  - `GET /api/clubs/{clubPublicId}`가 `skillTiers: [{id, name, level}]`를 `level` 내림차순으로 반환하는지 검증한다.

- [ ] **Step 2: 테스트가 실패하는지 확인한다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`
  - 기대 결과: 등급 DTO와 저장 로직이 없어 실패.

- [ ] **Step 3: 등급 교체 서비스를 구현한다.**
  - 요청 등급은 nullable `id`와 `name`을 가진 목록으로 받고, 목록 위치에 따라 `level = size - index`를 계산한다.
  - 기존 ID는 반드시 해당 클럽 소유인지 확인하고, 새 항목은 생성한다.
  - 요청에서 빠진 기존 등급은 그 등급을 참조하던 클럽원의 `skillTier`를 `null`로 바꾼 후 삭제한다.
  - 모든 변경은 `ClubCommandService.createClub` 및 `updateClub` 트랜잭션 안에서 수행한다.
  - 생성 응답은 기존처럼 `publicId`만 유지하고, 상세 조회 응답에만 등급 목록을 추가한다.

- [ ] **Step 4: 서비스와 API 테스트를 통과시킨다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`
  - 기대 결과: PASS.

## Task 4: 클럽원 등급 선택과 skillNote 제거

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubMemberCreateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubMemberUpdateRequest.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/dto/ClubMemberResponse.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/service/ClubMemberCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/club/service/ClubQueryService.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/club/service/ClubMemberCommandServiceTest.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/club/api/ClubControllerTest.java`

- [ ] **Step 1: 실패하는 클럽원 등급 테스트를 작성한다.**
  - `skillTierId=null`인 클럽원이 미정으로 생성·수정되는지 검증한다.
  - 같은 클럽 등급은 배정되고, 다른 클럽 등급 ID는 400 또는 404로 거부되는지 검증한다.
  - 등급 삭제 뒤 해당 등급의 활성·비활성 클럽원이 모두 미정으로 전환되는지 검증한다.
  - JSON 요청·응답에 `skillNote`가 없고 `skillTierId` 및 등급 정보가 있는지 검증한다.

- [ ] **Step 2: 테스트가 실패하는지 확인한다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`
  - 기대 결과: `skillTierId` 계약과 소속 검증이 없어 실패.

- [ ] **Step 3: 클럽원 명령과 응답을 구현한다.**
  - nullable `skillTierId`를 해석하는 공통 메서드에서 해당 클럽의 등급만 반환한다.
  - `ClubMember` 생성·수정에 선택된 `ClubSkillTier`를 전달한다.
  - 응답에는 nullable `skillTierId`, `skillTierName`, `skillTierLevel`을 추가하고 `skillNote`를 삭제한다.
  - 클럽원 검색에서 기존 실력 메모 조건을 제거한다.

- [ ] **Step 4: 클럽원 관련 테스트를 통과시킨다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubQueryServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest`
  - 기대 결과: PASS.

## Task 5: 클럽 생성·수정 및 클럽원 화면 연결

**Files:**
- Modify: `src/tennisFolio/src/utils/clubApi.js`
- Modify: `src/tennisFolio/src/page/club/clubPageUtils.js`
- Modify: `src/tennisFolio/src/page/club/ClubCreatePage.jsx`
- Modify: `src/tennisFolio/src/page/club/ClubEditPage.jsx`
- Modify: `src/tennisFolio/src/components/club/ClubCreateView.jsx`
- Modify: `src/tennisFolio/src/components/club/ClubEditView.jsx`
- Modify: `src/tennisFolio/src/components/club/ClubMemberForm.jsx`
- Modify: `src/tennisFolio/src/components/club/ClubDetailView.jsx`
- Modify: `src/tennisFolio/src/page/club/ClubMemberCreatePage.jsx`
- Modify: `src/tennisFolio/src/page/club/ClubMemberEditPage.jsx`
- Modify: `src/tennisFolio/src/page/Club.css`

- [ ] **Step 1: 클럽 폼 상태와 API payload를 확장한다.**
  - 빈 클럽 폼에 `skillTiers: []`를 추가한다.
  - 상세 응답의 등급 목록을 `level` 내림차순으로 정규화하고, 생성·수정 payload에 `{id?, name}` 목록을 담는다.

- [ ] **Step 2: 생성·수정 화면에 클럽 등급 편집 섹션을 추가한다.**
  - 등급 추가 버튼은 10개 미만일 때만 새 빈 항목을 추가한다.
  - 각 항목은 이름 입력, 위·아래 이동, 삭제 버튼을 제공한다.
  - 등급 이름 입력은 10자로 제한하고, 저장 전 빈 이름·중복 이름을 화면에서 안내하되 서버 검증을 대체하지 않는다.
  - Competition 디자인 시스템의 기존 CSS 변수만 사용해 Club 화면과 같은 카드·입력 스타일을 유지한다.

- [ ] **Step 3: 클럽원 등록·수정 화면을 등급 선택으로 바꾼다.**
  - `skillNote` 입력을 삭제한다.
  - 현재 클럽의 `skillTiers`를 `level` 내림차순으로 표시하는 select와 `미정` 기본 옵션을 추가한다.
  - 목록 행에는 선택된 등급 이름 또는 `미정`을 표시한다.

- [ ] **Step 4: 브라우저에서 수동으로 검증한다.**
  - 프런트엔드 테스트와 npm 빌드는 프로젝트 규칙상 명시적 요청 전까지 실행하지 않는다.
  - 로그인한 관리자로 클럽 생성 화면에서 등급 없이 저장, 4개 등급 추가 후 저장, 순서 변경 후 수정 저장을 확인한다.
  - 클럽원 등록·수정에서 등급과 미정을 각각 저장하고 목록 표시를 확인한다.
  - 배정된 등급을 삭제한 뒤 관련 클럽원이 미정으로 바뀌는지 확인한다.

## Task 6: 문서 정합성과 최종 검증

**Files:**
- Modify: `docs/superpowers/specs/2026-07-30-club-skill-tier-design.md`
- Modify: `docs/superpowers/plans/2026-07-30-club-skill-tier.md`

- [ ] **Step 1: 설계 문서와 구현 API를 대조한다.**
  - `skillNote` 제거, 0~10개 제한, N~1 `level`, 미정, 삭제 시 일괄 미정 처리, API 응답 범위가 코드와 일치하는지 확인한다.

- [ ] **Step 2: 백엔드 회귀 검증을 실행한다.**
  - 실행: `rtk .\\gradlew.bat test --tests com.tennisfolio.Tennisfolio.club.service.ClubCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubMemberCommandServiceTest --tests com.tennisfolio.Tennisfolio.club.service.ClubQueryServiceTest --tests com.tennisfolio.Tennisfolio.club.api.ClubControllerTest --tests com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepositoryTest`
  - 기대 결과: PASS.
  - 이 명령은 프로젝트 규칙에 따라 구현 시 사용자가 실행 승인을 준 경우에만 실행한다.

- [ ] **Step 3: 계획 및 설계 문서의 완료 상태를 갱신한다.**
  - 완료한 항목을 `[x]`로 표시하고, 실제 검증 명령과 결과만 기록한다.
  - 커밋은 프로젝트 규칙에 따라 사용자가 명시적으로 요청하고 확인한 경우에만 수행한다.
