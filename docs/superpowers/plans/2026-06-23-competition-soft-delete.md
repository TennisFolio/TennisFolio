# Competition Soft Delete Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 로그인 사용자가 본인 소유 Competition을 삭제하면 DB에는 보존하되 모든 일반 조회와 운영 API에서 접근할 수 없게 만든다.

**Architecture:** `tb_competition.DEL_DT`를 soft delete 기준으로 사용하고, Java 엔티티에는 `deletedAt` 필드를 둔다. `publicId` 기반 조회는 활성 Competition 조회 경로를 사용하고, 삭제 API만 소유자 기준 조회를 사용해 멱등적으로 삭제한다. 프론트는 내 경기 목록에서 삭제 버튼을 제공하고 성공 시 항목을 제거한다.

**Tech Stack:** Spring Boot, Spring MVC, Spring Security, Spring Data JPA, JUnit 5, Mockito, AssertJ, React, Axios.

---

## Working Notes

- 이 저장소 규칙상 사용자가 명시적으로 요청하지 않으면 커밋하지 않는다.
- Java/Gradle 테스트와 npm 빌드는 사용자가 검증 실행을 요청했을 때만 실행한다.
- DB 마이그레이션 도구는 현재 확인되지 않았다. 로컬은 `spring.jpa.hibernate.ddl-auto=update`가 있으므로 엔티티 변경으로 로컬 컬럼 생성은 가능하지만, 운영 반영용 SQL은 구현 결과 설명에 반드시 남긴다.
- 한국어 문서 일부는 콘솔에서 인코딩이 깨져 보일 수 있다. 깨진 문구를 근거로 요구사항을 추론하지 않는다.

## File Structure

- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
  - `deletedAt`, `delete(LocalDateTime)`, `isDeleted()`를 담당한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/repository/CompetitionRepository.java`
  - 활성 Competition 조회, 소유자 삭제 조회, 내 목록 조회 조건을 담당한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryService.java`
  - 상세/결과 조회에서 삭제된 Competition을 숨긴다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
  - 소유자 삭제 command를 담당한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
  - `DELETE /api/auth/me/competitions/{publicId}` 엔드포인트를 노출한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
  - 새 삭제 API를 인증 필수 경로에 포함한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionAdminAuthorizationService.java`
  - 삭제된 Competition에서 관리자 비밀번호 설정/로그인을 차단한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionEntryQueryService.java`
  - 삭제된 Competition의 참가자 조회를 차단한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionEntryCommandService.java`
  - 삭제된 Competition의 참가자 추가/수정을 차단한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionGameCommandService.java`
  - 삭제된 Competition의 게임 운영 API를 차단한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/MatchingTestFixtures.java`
  - 삭제된 Competition fixture를 만든다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
  - 삭제 API 컨트롤러 위임과 `204` 응답을 검증한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`
  - 삭제된 Competition 목록 제외와 상세/결과 차단을 검증한다.
- Modify/Create: service unit tests under `src/test/java/com/tennisfolio/Tennisfolio/matching/service`
  - command, admin authorization, entry/game command 차단을 검증한다.
- Modify: `src/tennisFolio/src/utils/authApi.js`
  - 내 Competition 삭제 API 함수를 추가한다.
- Modify: `src/tennisFolio/src/page/MyCompetitions.jsx`
  - 삭제 버튼, 확인창, 목록 제거, 오류 메시지를 담당한다.
- Modify: `src/tennisFolio/src/page/MyCompetitions.css`
  - danger 버튼과 항목 내부 레이아웃을 담당한다.

---

### Task 1: Entity And Repository Soft Delete Contract

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/repository/CompetitionRepository.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/MatchingTestFixtures.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`

- [ ] **Step 1: Add failing repository/list test**

Add this test to `CompetitionQueryServiceOwnedCompetitionTest`.

```java
@Test
void getOwnedCompetitions_excludesDeletedCompetitions() {
    Competition active = competitionRepository.save(new Competition(
            "active",
            4,
            4,
            2,
            1,
            136L,
            Competition.CompetitionMode.CLUB_SESSION,
            10L
    ));
    Competition softDeletedCompetition = competitionRepository.save(new Competition(
            "soft-deleted",
            4,
            4,
            2,
            1,
            136L,
            Competition.CompetitionMode.CLUB_SESSION,
            10L
    ));
    softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

    List<CompetitionSummaryResponse> response =
            competitionQueryService.getOwnedCompetitions(10L);

    assertThat(response)
            .extracting(CompetitionSummaryResponse::getPublicId)
            .containsExactly(active.getPublicId());
}
```

- [ ] **Step 2: Record focused test command**

Do not run this command unless the user explicitly asks for test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest
```

Expected before implementation: compile failure because `Competition.delete(LocalDateTime)` does not exist.

- [ ] **Step 3: Add `deletedAt` to `Competition`**

Update `Competition.java`.

```java
import java.time.LocalDateTime;
import java.util.UUID;
```

Add the field below `ownerUserId`.

```java
@Column(name = "DEL_DT")
private LocalDateTime deletedAt;
```

Add methods near the other domain methods.

```java
public void delete(LocalDateTime deletedAt) {
    if (this.deletedAt == null) {
        this.deletedAt = deletedAt;
    }
}

public boolean isDeleted() {
    return deletedAt != null;
}
```

- [ ] **Step 4: Add repository methods**

Update `CompetitionRepository.java`.

```java
Optional<Competition> findByPublicIdAndDeletedAtIsNull(String publicId);

Optional<Competition> findByPublicIdAndOwnerUserId(String publicId, Long ownerUserId);
```

Replace the owned list query with:

```java
@Query("""
        SELECT c
        FROM Competition c
        WHERE c.ownerUserId = :ownerUserId
          AND c.deletedAt IS NULL
        ORDER BY c.createDt DESC, c.id DESC
        """)
List<Competition> findByOwnerUserIdOrderByCreateDtDesc(@Param("ownerUserId") Long ownerUserId);
```

- [ ] **Step 5: Add deleted fixture helper**

Update `MatchingTestFixtures.java`.

```java
public static Competition deletedOwnedCompetition(
        Long id,
        String publicId,
        Long ownerUserId,
        Competition.CompetitionMode mode
) {
    Competition competition = ownedCompetition(id, publicId, ownerUserId, mode);
    competition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));
    return competition;
}
```

- [ ] **Step 6: Record focused test command**

Do not run this command unless the user explicitly asks for test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest
```

Expected after implementation: PASS.

---

### Task 2: Delete Command And Auth API

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
- Create: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceDeleteTest.java`

- [ ] **Step 1: Create failing command service tests**

Create `CompetitionCommandServiceDeleteTest.java`.

```java
package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.deletedOwnedCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.ownedCompetition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceDeleteTest {

    @Mock
    TennisMatchScheduler scheduler;

    @Mock
    CompetitionService competitionService;

    @Mock
    CompetitionEntryCommandService competitionEntryCommandService;

    @Mock
    GameService gameService;

    @Mock
    CompetitionStatService competitionStatService;

    @Mock
    CompetitionAdminTokenService competitionAdminTokenService;

    @Mock
    CompetitionRepository competitionRepository;

    @InjectMocks
    CompetitionCommandService service;

    @Test
    void deleteOwnedCompetition_setsDeletedAt() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 10L))
                .thenReturn(Optional.of(competition));

        service.deleteOwnedCompetition("public-id", 10L);

        assertThat(competition.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteOwnedCompetition_isIdempotentForAlreadyDeletedCompetition() {
        Competition competition = deletedOwnedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        java.time.LocalDateTime originalDeletedAt = competition.getDeletedAt();
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 10L))
                .thenReturn(Optional.of(competition));

        service.deleteOwnedCompetition("public-id", 10L);

        assertThat(competition.getDeletedAt()).isEqualTo(originalDeletedAt);
    }

    @Test
    void deleteOwnedCompetition_throwsNotFoundForOtherOwner() {
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOwnedCompetition("public-id", 20L))
                .isInstanceOf(NotFoundException.class);
    }
}
```

- [ ] **Step 2: Implement command service deletion**

Add `CompetitionRepository` as a dependency to `CompetitionCommandService`.

```java
private final CompetitionRepository competitionRepository;
```

Add it to the constructor parameters and assignment.

```java
CompetitionRepository competitionRepository
```

```java
this.competitionRepository = competitionRepository;
```

Add imports:

```java
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import java.time.LocalDateTime;
```

Add the method:

```java
@Transactional
public void deleteOwnedCompetition(String publicId, Long ownerUserId) {
    Competition competition = competitionRepository
            .findByPublicIdAndOwnerUserId(publicId, ownerUserId)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    competition.delete(LocalDateTime.now());
}
```

- [ ] **Step 3: Add failing controller test**

Add to `AuthControllerTest`.

```java
@Test
void deleteMyCompetition_deletesCurrentUsersCompetition() {
    Authentication authentication =
            new UsernamePasswordAuthenticationToken(1L, null, List.of());

    ResponseEntity<Void> response =
            authController.deleteMyCompetition(authentication, "public-id");

    verify(competitionCommandService).deleteOwnedCompetition("public-id", 1L);
    assertThat(response.getStatusCode().value()).isEqualTo(204);
}
```

Add the mock field:

```java
@Mock
CompetitionCommandService competitionCommandService;
```

Add the import:

```java
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
```

- [ ] **Step 4: Implement controller endpoint**

Update `AuthController.java`.

Add imports:

```java
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import org.springframework.web.bind.annotation.DeleteMapping;
```

Add field and constructor parameter:

```java
private final CompetitionCommandService competitionCommandService;
```

```java
CompetitionCommandService competitionCommandService
```

```java
this.competitionCommandService = competitionCommandService;
```

Add endpoint:

```java
@DeleteMapping("/me/competitions/{publicId}")
public ResponseEntity<Void> deleteMyCompetition(
        Authentication authentication,
        @PathVariable String publicId
) {
    Long userId = (Long) authentication.getPrincipal();
    competitionCommandService.deleteOwnedCompetition(publicId, userId);
    return ResponseEntity.noContent().build();
}
```

- [ ] **Step 5: Require authentication for delete route**

Update `SecurityConfig.java`.

```java
.requestMatchers(
        "/api/auth/me",
        "/api/auth/profile",
        "/api/auth/me/competitions",
        "/api/auth/me/competitions/*"
).authenticated()
```

- [ ] **Step 6: Record focused test command**

Do not run this command unless the user explicitly asks for test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceDeleteTest --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest
```

Expected after implementation: PASS.

---

### Task 3: Hide Deleted Competition From Public Queries

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryService.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`

- [ ] **Step 1: Add failing query tests**

Add to `CompetitionQueryServiceOwnedCompetitionTest`.

```java
@Test
void getCompetition_throwsNotFoundForDeletedCompetition() {
    Competition softDeletedCompetition = competitionRepository.save(new Competition(
            "soft-deleted",
            4,
            4,
            2,
            1,
            136L,
            Competition.CompetitionMode.CLUB_SESSION,
            10L
    ));
    softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

    assertThatThrownBy(() -> competitionQueryService.getCompetition(softDeletedCompetition.getPublicId(), 10L))
            .isInstanceOf(com.tennisfolio.Tennisfolio.exception.NotFoundException.class);
}

@Test
void getCompetitionResult_throwsNotFoundForDeletedCompetition() {
    Competition softDeletedCompetition = competitionRepository.save(new Competition(
            "soft-deleted",
            4,
            4,
            2,
            1,
            136L,
            Competition.CompetitionMode.CLUB_SESSION,
            10L
    ));
    softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

    assertThatThrownBy(() -> competitionQueryService.getCompetitionResult(softDeletedCompetition.getPublicId()))
            .isInstanceOf(com.tennisfolio.Tennisfolio.exception.NotFoundException.class);
}
```

Add import:

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

- [ ] **Step 2: Use active lookup in query service**

In `CompetitionQueryService`, replace both `competitionRepository.findByPublicId(publicId)` calls with:

```java
findActiveCompetition(publicId)
```

Add helper:

```java
private Competition findActiveCompetition(String publicId) {
    return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
}
```

The start of `getCompetition` should become:

```java
Competition competition = findActiveCompetition(publicId);
```

The start of `getCompetitionResult` should become:

```java
Competition competition = findActiveCompetition(publicId);
```

- [ ] **Step 3: Record focused query test command**

Do not run this command unless the user explicitly asks for test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest
```

Expected after implementation: PASS.

---

### Task 4: Block Deleted Competition In Admin, Entry, And Game Services

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionAdminAuthorizationService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionEntryQueryService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionEntryCommandService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionGameCommandService.java`
- Modify existing tests in `src/test/java/com/tennisfolio/Tennisfolio/matching/service`

- [ ] **Step 1: Replace admin service lookup**

In `CompetitionAdminAuthorizationService`, change `findCompetition`:

```java
private Competition findCompetition(String publicId) {
    return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
            .orElseThrow(() -> new IllegalArgumentException("Competition not found"));
}
```

In `isCurrentUserOwner`, use active lookup:

```java
return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
        .map(competition -> currentUserId.equals(competition.getOwnerUserId()))
        .orElse(false);
```

- [ ] **Step 2: Replace entry query lookup**

In `CompetitionEntryQueryService`, replace:

```java
competitionRepository.findByPublicId(publicId)
```

with:

```java
competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
```

- [ ] **Step 3: Replace entry command lookups**

In `CompetitionEntryCommandService`, replace both:

```java
competitionRepository.findByPublicId(publicId)
```

with:

```java
competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
```

- [ ] **Step 4: Replace game command lookups**

In `CompetitionGameCommandService`, replace every publicId lookup:

```java
competitionRepository.findByPublicId(publicId)
```

with:

```java
competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
```

Affected methods:

- `createNextCourtGame`
- `updateGameStatus`
- `deleteGame`
- `updateCourtCount`
- `updateGameEntries`
- `updateGameScore`

- [ ] **Step 5: Update affected mocks**

Existing unit tests that stub `competitionRepository.findByPublicId("public-id")` for these services must stub the active lookup instead.

Use this replacement in affected tests:

```java
when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
        .thenReturn(Optional.of(competition));
```

For not-found tests, use:

```java
when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
        .thenReturn(Optional.empty());
```

- [ ] **Step 6: Add one deleted-access regression per service group**

Add one deleted Competition test to each existing service test class touched by this task.

Admin authorization example:

```java
@Test
void login_throwsWhenCompetitionIsDeleted() {
    when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login("public-id", "1234"))
            .isInstanceOf(IllegalArgumentException.class);
}
```

Entry command example:

```java
@Test
void createCompetitionEntry_throwsNotFoundWhenCompetitionIsDeleted() {
    when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createCompetitionEntry(
            "public-id",
            "admin-token",
            createEntryRequest("player", "MALE")
    )).isInstanceOf(NotFoundException.class);
}
```

Game command example:

```java
@Test
void updateGameScore_throwsNotFoundWhenCompetitionIsDeleted() {
    when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateGameScore(
            "public-id",
            10L,
            "admin-token",
            new GameScoreUpdateRequest()
    )).isInstanceOf(NotFoundException.class);
}
```

- [ ] **Step 7: Record focused service test command**

Do not run this command unless the user explicitly asks for test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionAdminAuthorizationServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryCommandServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionGameCommandServiceTest
```

Expected after implementation: PASS.

---

### Task 5: Frontend Delete Action

**Files:**
- Modify: `src/tennisFolio/src/utils/authApi.js`
- Modify: `src/tennisFolio/src/page/MyCompetitions.jsx`
- Modify: `src/tennisFolio/src/page/MyCompetitions.css`

- [ ] **Step 1: Add API helper**

Update `authApi.js`.

```js
export const deleteMyCompetition = (publicId) =>
  apiRequestSilent.delete(`/api/auth/me/competitions/${publicId}`);
```

- [ ] **Step 2: Import delete helper**

Update `MyCompetitions.jsx`.

```js
import { deleteMyCompetition, getMyCompetitions } from '../utils/authApi';
```

- [ ] **Step 3: Add component state**

Inside `MyCompetitions`, add:

```js
const [deletingPublicId, setDeletingPublicId] = useState('');
const [actionMessage, setActionMessage] = useState('');
```

- [ ] **Step 4: Add delete handler**

Add inside `MyCompetitions`.

```js
const handleDeleteCompetition = async (event, competition) => {
  event.stopPropagation();

  const confirmed = window.confirm(
    '삭제하면 공유 링크와 경기 운영 화면에 더 이상 접근할 수 없습니다. 삭제할까요?'
  );
  if (!confirmed) {
    return;
  }

  try {
    setDeletingPublicId(competition.publicId);
    await deleteMyCompetition(competition.publicId);
    setCompetitions((current) =>
      current.filter((item) => item.publicId !== competition.publicId)
    );
    setActionMessage('경기를 삭제했어요.');
    setErrorMessage('');
  } catch (error) {
    setActionMessage('');
    setErrorMessage(
      error.response?.data?.message || '경기를 삭제하지 못했어요.'
    );
  } finally {
    setDeletingPublicId('');
  }
};
```

- [ ] **Step 5: Render action message**

Below error rendering, add:

```jsx
{!isLoading && actionMessage && (
  <p className="my-competitions-state success">{actionMessage}</p>
)}
```

- [ ] **Step 6: Add delete button in each item**

Inside each `.my-competition-item`, add a nested action area after created date or stats.

```jsx
<span className="my-competition-actions">
  <button
    type="button"
    className="my-competition-delete-button"
    disabled={deletingPublicId === competition.publicId}
    onClick={(event) => handleDeleteCompetition(event, competition)}
  >
    {deletingPublicId === competition.publicId ? '삭제 중' : '삭제'}
  </button>
</span>
```

Convert the outer item from `button` to `article` with explicit buttons. This avoids invalid nested buttons and keeps the delete button from triggering detail navigation.

```jsx
<article className="my-competition-item" key={competition.publicId}>
  <button
    type="button"
    className="my-competition-open-button"
    onClick={() => navigate(`/competitions/${competition.publicId}`)}
  >
    ...
  </button>
  <button
    type="button"
    className="my-competition-delete-button"
    disabled={deletingPublicId === competition.publicId}
    onClick={(event) => handleDeleteCompetition(event, competition)}
  >
    {deletingPublicId === competition.publicId ? '삭제 중' : '삭제'}
  </button>
</article>
```

- [ ] **Step 7: Add CSS using theme variables**

Update `MyCompetitions.css`.

```css
.my-competitions-state.success {
  border-color: var(--competition-ok-border);
  color: var(--competition-ok);
  background: var(--competition-ok-bg);
}

.my-competition-item {
  display: grid;
  gap: 8px;
}

.my-competition-open-button {
  display: grid;
  width: 100%;
  gap: 7px;
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
}

.my-competition-actions {
  display: flex;
  justify-content: flex-end;
}

.my-competition-delete-button {
  min-width: 64px;
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--competition-danger-border);
  border-radius: 6px;
  background: var(--competition-danger-bg);
  color: var(--competition-danger);
  font-size: 12px;
  font-weight: 900;
}

.my-competition-delete-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
```

- [ ] **Step 8: Record manual UI inspection command**

Do not run this command unless the user explicitly asks for frontend verification. If allowed, start the Vite dev server and inspect `/me/competitions`.

```powershell
npm run dev
```

Expected: each item has a delete button, the item still opens detail via the main area, delete confirmation appears, success removes the item.

---

### Task 6: Final Verification Sweep

**Files:**
- Review all modified files.

- [ ] **Step 1: Search for unsafe publicId lookups**

```powershell
rg -n "findByPublicId\\(" src/main/java/com/tennisfolio/Tennisfolio/matching src/main/java/com/tennisfolio/Tennisfolio/user -S
```

Expected: remaining `findByPublicId(` calls are only for creation-time/admin-token-compatible cases where deleted data must be visible. For this feature, public/operation paths should use `findByPublicIdAndDeletedAtIsNull`.

- [ ] **Step 2: Search for active lookup coverage**

```powershell
rg -n "findByPublicIdAndDeletedAtIsNull|findByPublicIdAndOwnerUserId|DEL_DT|deletedAt" src/main/java src/test/java -S
```

Expected: entity, repository, delete command, public query services, admin/entry/game services, and tests reference the new soft delete contract.

- [ ] **Step 3: Record backend test command**

Do not run this command unless the user explicitly asks for backend test verification:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceDeleteTest --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionAdminAuthorizationServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryCommandServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionGameCommandServiceTest
```

Expected: PASS.

- [ ] **Step 4: Record DB operation note**

In final handoff, include this SQL for environments that do not auto-update schema:

```sql
ALTER TABLE tb_competition ADD COLUMN DEL_DT DATETIME NULL;
```

If the production database uses a different datetime type, adapt only the type while keeping the column name `DEL_DT`.

- [ ] **Step 5: Do not commit unless explicitly requested**

Check status:

```powershell
git status --short
```

Expected: changed files are visible and no commit has been created unless the user asked for one.
