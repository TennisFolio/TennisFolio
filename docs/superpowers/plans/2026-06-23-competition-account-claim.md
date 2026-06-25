# Competition Account Claim Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 비로그인으로 만든 Competition을 관리자 토큰 보유자가 로그인 후 자기 계정에 저장할 수 있게 한다.

**Architecture:** 기존 단일 `Competition.ownerUserId` 모델을 유지한다. 백엔드는 `POST /api/auth/me/competitions/{publicId}/claim`에서 로그인 사용자와 `X-Competition-Admin-Token`을 검증한 뒤 owner가 비어 있으면 현재 사용자 id를 저장한다. 프론트엔드는 Competition 상세 화면에 `내 계정에 저장` 버튼을 추가하고, 저장 버튼에서 시작한 로그인만 `claimAfterLogin=1`로 자동 저장을 이어간다.

**Tech Stack:** Spring Boot, Spring Security, JPA, JUnit 5, Mockito, React, React Router, Axios, CSS variables.

---

## 사전 확인

- 관련 설계: `docs/superpowers/specs/2026-06-23-competition-account-claim-design.md`
- `src/main/java/com/tennisfolio/Tennisfolio/matching/ENTITY_DESIGN.md`는 현재 워크스페이스에서 찾을 수 없었다. 구현 전 파일이 생겼다면 먼저 읽는다.
- 프로젝트 규칙상 Java/Gradle, npm 빌드/테스트는 사용자가 명시적으로 허용했을 때만 실행한다.
- 커밋은 사용자가 명시적으로 요청하지 않는 한 하지 않는다.

## 파일 구조

- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
  - owner claim 도메인 메서드를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionDetailResponse.java`
  - 화면이 owner 존재 여부를 알 수 있도록 `ownerUserIdSet`을 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
  - claim 유스케이스를 구현한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
  - claim API를 노출한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
  - claim URL 인증 규칙을 명확히 한다.
- Test: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceClaimTest.java`
  - claim 도메인 규칙을 검증한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
  - 컨트롤러가 user id와 admin token을 서비스에 전달하는지 검증한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`
  - 상세 응답의 `ownerUserIdSet`과 `ownedByCurrentUser`를 검증한다.
- Modify: `src/tennisFolio/src/utils/authApi.js`
  - claim API 클라이언트를 추가한다.
- Modify: `src/tennisFolio/src/page/CompetitionDetail.jsx`
  - 저장 버튼, claimAfterLogin 처리, 성공/실패 메시지를 추가한다.
- Modify: `src/tennisFolio/src/page/CompetitionDetail.css`
  - 저장 배너 스타일을 추가한다.
- Create: `src/tennisFolio/src/page/competitionAccountClaim.test.mjs`
  - 정적 테스트로 주요 문자열/흐름이 존재하는지 검증한다.

---

### Task 1: 상세 응답에 owner 존재 여부 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionDetailResponse.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`

- [ ] **Step 1: 실패 테스트 추가**

`CompetitionQueryServiceOwnedCompetitionTest`에 아래 테스트를 추가한다.

```java
@Test
void getCompetition_marksOwnerPresenceAndCurrentUserOwnership() {
    Competition competition = ownedCompetition(
            1L,
            "public-id",
            10L,
            Competition.CompetitionMode.CLUB_SESSION
    );
    CompetitionStat stat = new CompetitionStat(competition, 0, 0, 0);

    when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
            .thenReturn(Optional.of(competition));
    when(competitionStatRepository.findByCompetition(competition))
            .thenReturn(Optional.of(stat));
    when(gameEntryRepository.findByGameCompetitionOrderByGameRoundAscGameCourtAscSlotAsc(competition))
            .thenReturn(List.of());

    CompetitionDetailResponse response =
            service.getCompetition("public-id", 10L);

    assertThat(response.getOwnerUserIdSet()).isTrue();
    assertThat(response.getOwnedByCurrentUser()).isTrue();
}
```

- [ ] **Step 2: 실패 확인**

사용자 허용 시 실행:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest
```

Expected: `getOwnerUserIdSet()` 메서드가 없어 컴파일 실패한다.

- [ ] **Step 3: DTO 수정**

`CompetitionDetailResponse`에 `ownerUserIdSet` 필드를 `ownedByCurrentUser` 앞에 추가한다.

```java
private Boolean ownerUserIdSet;
private Boolean ownedByCurrentUser;
```

생성자 인자 위치에 아래 값을 추가한다.

```java
competition.getOwnerUserId() != null,
currentUserId != null && currentUserId.equals(competition.getOwnerUserId()),
```

최종 반환부는 이 순서를 유지한다.

```java
return new CompetitionDetailResponse(
        competition.getPublicId(),
        competition.getName(),
        competition.getMaleCount(),
        competition.getFemaleCount(),
        competition.getCourtCount(),
        competition.getRounds(),
        competition.getStatus().name(),
        competition.getMode() == null ? Competition.CompetitionMode.FIXED_SCHEDULE.name() : competition.getMode().name(),
        competition.hasAdminPassword(),
        competition.getOwnerUserId() != null,
        currentUserId != null && currentUserId.equals(competition.getOwnerUserId()),
        competition.getCreateDt(),
        CompetitionStatResponse.from(stat),
        gameEntriesByGameId.values().stream()
                .map(entries -> GameResponse.from(entries.get(0).getGame(), entries))
                .toList()
);
```

- [ ] **Step 4: 통과 확인**

사용자 허용 시 Step 2의 테스트 명령을 다시 실행한다.

Expected: 테스트 통과.

---

### Task 2: Competition claim 도메인 규칙 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceClaimTest.java`

- [ ] **Step 1: 실패 테스트 파일 생성**

`CompetitionCommandServiceClaimTest.java`를 생성한다.

```java
package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.ownedCompetition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceClaimTest {

    @Mock TennisMatchScheduler scheduler;
    @Mock CompetitionService competitionService;
    @Mock CompetitionEntryCommandService competitionEntryCommandService;
    @Mock GameService gameService;
    @Mock CompetitionStatService competitionStatService;
    @Mock CompetitionAdminTokenService competitionAdminTokenService;
    @Mock CompetitionRepository competitionRepository;

    CompetitionCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionCommandService(
                scheduler,
                competitionService,
                competitionEntryCommandService,
                gameService,
                competitionStatService,
                competitionAdminTokenService,
                competitionRepository
        );
    }

    @Test
    void claimCompetition_setsOwnerWhenCompetitionHasNoOwner() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                null,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));
        when(competitionAdminTokenService.validateAndGetPublicId("admin-token"))
                .thenReturn("public-id");

        service.claimCompetition("public-id", 10L, "admin-token");

        verify(competitionAdminTokenService).validateAndGetPublicId("admin-token");
        assertThat(competition.getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void claimCompetition_isIdempotentForCurrentOwner() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));

        service.claimCompetition("public-id", 10L, "admin-token");

        assertThat(competition.getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void claimCompetition_throwsConflictWhenAnotherOwnerExists() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                20L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void claimCompetition_throwsNotFoundWhenCompetitionIsDeleted() {
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void claimCompetition_throwsForbiddenWhenTokenPublicIdDiffers() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                null,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));
        when(competitionAdminTokenService.validateAndGetPublicId("admin-token"))
                .thenReturn("other-public-id");

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }
}
```

- [ ] **Step 2: 실패 확인**

사용자 허용 시 실행:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceClaimTest
```

Expected: `claimCompetition` 또는 `claimOwner`가 없어 컴파일 실패한다.

- [ ] **Step 3: Competition 도메인 메서드 추가**

`Competition.java`에 아래 메서드를 추가한다.

```java
public void claimOwner(Long ownerUserId) {
    if (this.ownerUserId == null) {
        this.ownerUserId = ownerUserId;
    }
}
```

- [ ] **Step 4: CompetitionCommandService 구현**

import를 추가한다.

```java
import org.springframework.http.HttpStatus;
```

`CompetitionCommandService`에 메서드를 추가한다.

```java
@Transactional
public void claimCompetition(String publicId, Long ownerUserId, String adminToken) {
    Competition competition = competitionRepository
            .findByPublicIdAndDeletedAtIsNull(publicId)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

    if (ownerUserId.equals(competition.getOwnerUserId())) {
        return;
    }

    if (competition.getOwnerUserId() != null) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "이미 다른 계정에 저장된 경기입니다."
        );
    }

    String tokenPublicId = competitionAdminTokenService.validateAndGetPublicId(adminToken);
    if (!publicId.equals(tokenPublicId)) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "관리자 권한이 올바르지 않습니다. 다시 로그인해 주세요."
        );
    }

    competition.claimOwner(ownerUserId);
}
```

- [ ] **Step 5: 통과 확인**

사용자 허용 시 Step 2의 테스트 명령을 다시 실행한다.

Expected: 테스트 통과.

---

### Task 3: claim API 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`

- [ ] **Step 1: 실패 테스트 추가**

`AuthControllerTest`에 테스트를 추가한다.

```java
@Test
void claimMyCompetition_claimsCompetitionForCurrentUser() {
    Authentication authentication =
            new UsernamePasswordAuthenticationToken(1L, null, List.of());

    ResponseEntity<Void> response =
            authController.claimMyCompetition(authentication, "public-id", "admin-token");

    verify(competitionCommandService)
            .claimCompetition("public-id", 1L, "admin-token");
    assertThat(response.getStatusCode().value()).isEqualTo(204);
}
```

- [ ] **Step 2: 실패 확인**

사용자 허용 시 실행:

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest
```

Expected: `claimMyCompetition` 메서드가 없어 컴파일 실패한다.

- [ ] **Step 3: AuthController 구현**

`AuthController.java`에 import를 추가한다.

```java
import org.springframework.web.bind.annotation.RequestHeader;
```

아래 메서드를 `deleteMyCompetition` 근처에 추가한다.

```java
@PostMapping("/me/competitions/{publicId}/claim")
public ResponseEntity<Void> claimMyCompetition(
        Authentication authentication,
        @PathVariable String publicId,
        @RequestHeader(value = "X-Competition-Admin-Token", required = false) String adminToken
) {
    Long userId = (Long) authentication.getPrincipal();
    competitionCommandService.claimCompetition(publicId, userId, adminToken);
    return ResponseEntity.noContent().build();
}
```

- [ ] **Step 4: SecurityConfig 확인 및 보강**

현재 `"/api/auth/me/competitions/*"`는 한 세그먼트까지만 명확하다. claim 경로까지 확실히 인증되도록 matcher를 추가한다.

```java
.requestMatchers(
        "/api/auth/me",
        "/api/auth/profile",
        "/api/auth/me/competitions",
        "/api/auth/me/competitions/*",
        "/api/auth/me/competitions/*/claim"
).authenticated()
```

- [ ] **Step 5: 통과 확인**

사용자 허용 시 Step 2의 테스트 명령을 다시 실행한다.

Expected: 테스트 통과.

---

### Task 4: 프론트 API 클라이언트 추가

**Files:**
- Modify: `src/tennisFolio/src/utils/authApi.js`
- Create: `src/tennisFolio/src/page/competitionAccountClaim.test.mjs`

- [ ] **Step 1: 실패 정적 테스트 생성**

`src/tennisFolio/src/page/competitionAccountClaim.test.mjs`를 생성한다.

```js
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const authApiSource = readFileSync(
  new URL('../utils/authApi.js', import.meta.url),
  'utf8',
);

test('auth API exposes competition claim request', () => {
  assert.match(authApiSource, /export const claimMyCompetition/);
  assert.match(authApiSource, /\/api\/auth\/me\/competitions\/\$\{publicId\}\/claim/);
  assert.match(authApiSource, /createAdminTokenHeaders\(adminToken\)/);
});
```

- [ ] **Step 2: 실패 확인**

사용자 허용 시 실행:

```powershell
node src/tennisFolio/src/page/competitionAccountClaim.test.mjs
```

Expected: `claimMyCompetition` 문자열이 없어 실패한다.

- [ ] **Step 3: authApi 구현**

`authApi.js`의 import를 수정한다.

```js
import { oauth_authorization_urls } from '@/constants';
import { apiRequestSilent } from './apiClient';
import { createAdminTokenHeaders } from './competitionEditToken';
```

`deleteMyCompetition` 아래에 추가한다.

```js
export const claimMyCompetition = (publicId, adminToken) =>
  apiRequestSilent.post(
    `/api/auth/me/competitions/${publicId}/claim`,
    null,
    {
      headers: createAdminTokenHeaders(adminToken),
    },
  );
```

- [ ] **Step 4: 통과 확인**

사용자 허용 시 Step 2의 테스트 명령을 다시 실행한다.

Expected: 테스트 통과.

---

### Task 5: Competition 상세 화면에 저장 배너 추가

**Files:**
- Modify: `src/tennisFolio/src/page/CompetitionDetail.jsx`
- Modify: `src/tennisFolio/src/page/CompetitionDetail.css`
- Modify: `src/tennisFolio/src/page/competitionAccountClaim.test.mjs`

- [ ] **Step 1: 실패 정적 테스트 확장**

`competitionAccountClaim.test.mjs`에 상세 화면 소스를 추가한다.

```js
const detailSource = readFileSync(
  new URL('./CompetitionDetail.jsx', import.meta.url),
  'utf8',
);

const detailCss = readFileSync(
  new URL('./CompetitionDetail.css', import.meta.url),
  'utf8',
);
```

테스트를 추가한다.

```js
test('competition detail renders account save action from claim flow', () => {
  assert.match(detailSource, /claimAfterLogin/);
  assert.match(detailSource, /claimMyCompetition/);
  assert.match(detailSource, /내 계정에 저장/);
  assert.match(detailSource, /이미 다른 계정에 저장된 경기입니다\./);
});

test('competition account save banner uses competition theme variables', () => {
  assert.match(detailCss, /\.competition-account-claim/);
  assert.match(detailCss, /var\(--competition-surface-accent\)/);
  assert.match(detailCss, /var\(--competition-border\)/);
});
```

- [ ] **Step 2: 실패 확인**

사용자 허용 시 실행:

```powershell
node src/tennisFolio/src/page/competitionAccountClaim.test.mjs
```

Expected: `claimAfterLogin` 또는 `내 계정에 저장` 문자열이 없어 실패한다.

- [ ] **Step 3: CompetitionDetail import 수정**

`CompetitionDetail.jsx`의 auth import에 `claimMyCompetition`, `getCurrentUser`, `loginWithProvider`를 포함한다. 기존 import 모양에 맞춰 추가한다.

```js
import {
  claimMyCompetition,
  getCurrentUser,
  loginWithProvider,
} from '../utils/authApi';
```

이미 `authApi` import가 있다면 위 함수만 추가한다.

- [ ] **Step 4: 상태와 계산값 추가**

`CompetitionDetail` 컴포넌트의 state 영역에 추가한다.

```js
const [accountClaimMessage, setAccountClaimMessage] = useState('');
const [accountClaimError, setAccountClaimError] = useState('');
const [isClaimingAccount, setIsClaimingAccount] = useState(false);
```

`requestedView` 근처에 추가한다.

```js
const searchParams = new URLSearchParams(location.search);
const requestedView = searchParams.get('view');
const claimAfterLogin = searchParams.get('claimAfterLogin') === '1';
const canShowAccountClaim =
  competition?.ownerUserIdSet === false && Boolean(adminToken);
```

기존 `requestedView` 선언이 이미 있으면 중복 선언하지 말고 위 형태로 교체한다.

- [ ] **Step 5: claim 함수 추가**

`rejectWithoutPermission` 아래 또는 `refreshCompetition` 위에 추가한다.

```js
const removeClaimAfterLoginParam = useCallback(() => {
  const params = new URLSearchParams(location.search);
  if (!params.has('claimAfterLogin')) {
    return;
  }
  params.delete('claimAfterLogin');
  const nextSearch = params.toString();
  navigate(
    {
      pathname: location.pathname,
      search: nextSearch ? `?${nextSearch}` : '',
    },
    { replace: true },
  );
}, [location.pathname, location.search, navigate]);

const claimCompetitionForAccount = useCallback(async () => {
  if (!adminToken) {
    setAccountClaimMessage('');
    setAccountClaimError('관리자 권한이 있어야 내 계정에 저장할 수 있습니다.');
    return;
  }

  try {
    setIsClaimingAccount(true);
    setAccountClaimMessage('');
    setAccountClaimError('');
    await claimMyCompetition(publicId, adminToken);
    setAccountClaimMessage('내 계정에 저장했어요.');
    await refreshCompetition();
  } catch (error) {
    setAccountClaimMessage('');
    if (error.response?.status === 409) {
      setAccountClaimError('이미 다른 계정에 저장된 경기입니다.');
    } else if (error.response?.status === 401) {
      setAccountClaimError('로그인 후 다시 시도해 주세요.');
    } else {
      setAccountClaimError(
        error.response?.data?.message || '내 계정에 저장하지 못했어요.',
      );
    }
  } finally {
    setIsClaimingAccount(false);
    removeClaimAfterLoginParam();
  }
}, [adminToken, publicId, refreshCompetition, removeClaimAfterLoginParam]);
```

- [ ] **Step 6: 저장 버튼 클릭 핸들러 추가**

`claimCompetitionForAccount` 아래에 추가한다.

```js
const handleAccountClaimClick = async () => {
  try {
    await getCurrentUser();
    await claimCompetitionForAccount();
  } catch (error) {
    if (error.response?.status === 401) {
      const params = new URLSearchParams(location.search);
      params.set('claimAfterLogin', '1');
      const redirectPath = `${location.pathname}?${params.toString()}`;
      sessionStorage.setItem('tennisfolio:postLoginRedirect', redirectPath);
      loginWithProvider(default_oauth_provider);
      return;
    }
    setAccountClaimMessage('');
    setAccountClaimError('로그인 상태를 확인하지 못했어요.');
  }
};
```

`default_oauth_provider` import가 없다면 기존 `Layout.jsx`가 쓰는 동일 상수를 import한다.

```js
import { default_oauth_provider } from '@/constants';
```

- [ ] **Step 7: claimAfterLogin effect 추가**

`fetchCompetition` effect 아래에 추가한다.

```js
useEffect(() => {
  if (!claimAfterLogin || !competition || isClaimingAccount) {
    return;
  }

  claimCompetitionForAccount();
}, [claimAfterLogin, competition, isClaimingAccount, claimCompetitionForAccount]);
```

- [ ] **Step 8: 배너 렌더링 추가**

hero content 안의 경고/포인트 영역 전후 중 하나에 추가한다. `competition-detail-hero-content` 내부에 둔다.

```jsx
{canShowAccountClaim && (
  <div className="competition-account-claim">
    <div>
      <strong>이 경기를 내 계정에 저장할 수 있어요.</strong>
      <p>저장하면 나중에 내 경기에서 다시 찾고 관리할 수 있습니다.</p>
    </div>
    <button
      type="button"
      disabled={isClaimingAccount}
      onClick={handleAccountClaimClick}
    >
      {isClaimingAccount ? '저장 중' : '내 계정에 저장'}
    </button>
  </div>
)}
{accountClaimMessage && (
  <div className="competition-account-claim-message success">
    {accountClaimMessage}
  </div>
)}
{accountClaimError && (
  <div className="competition-account-claim-message error">
    {accountClaimError}
  </div>
)}
```

- [ ] **Step 9: CSS 추가**

`CompetitionDetail.css`에 추가한다.

```css
.competition-account-claim {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--competition-border);
  border-radius: 8px;
  background: var(--competition-surface-accent);
}

.competition-account-claim strong {
  display: block;
  color: var(--competition-text-strong);
  font-size: 14px;
  font-weight: 900;
}

.competition-account-claim p {
  margin: 4px 0 0;
  color: var(--competition-text-body);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.4;
}

.competition-account-claim button {
  flex: 0 0 auto;
  min-width: 104px;
  height: 36px;
  border: 1px solid var(--competition-primary);
  border-radius: 8px;
  background: var(--competition-primary);
  color: var(--competition-text-inverse);
  font-size: 13px;
  font-weight: 900;
}

.competition-account-claim button:disabled {
  border-color: var(--competition-primary-disabled);
  background: var(--competition-primary-disabled);
  color: var(--competition-text-muted);
}

.competition-account-claim-message {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 800;
}

.competition-account-claim-message.success {
  border: 1px solid var(--competition-ok-border);
  background: var(--competition-ok-bg);
  color: var(--competition-ok);
}

.competition-account-claim-message.error {
  border: 1px solid var(--competition-danger-border);
  background: var(--competition-danger-bg);
  color: var(--competition-danger);
}

@media (max-width: 520px) {
  .competition-account-claim {
    align-items: stretch;
    flex-direction: column;
  }

  .competition-account-claim button {
    width: 100%;
  }
}
```

- [ ] **Step 10: 통과 확인**

사용자 허용 시 Step 2의 테스트 명령을 다시 실행한다.

Expected: 테스트 통과.

---

### Task 6: OAuth 로그인 후 redirect 연결 확인

**Files:**
- Modify: `src/tennisFolio/src/utils/authApi.js`
- Modify: `src/tennisFolio/src/Layout.jsx`
- Modify: `src/tennisFolio/src/Layout.test.mjs`

- [ ] **Step 1: 현재 OAuth 성공 redirect 동작 확인**

`OAuthLoginSuccessHandler`와 프론트 로그인 완료 처리가 이미 `sessionStorage`의 redirect를 읽는지 확인한다.

```powershell
rg -n "postLoginRedirect|redirect|OAuthLoginSuccessHandler|window.location|loginWithProvider" src/main/java src/tennisFolio/src
```

Expected: `tennisfolio:postLoginRedirect`를 읽는 코드가 없으면 다음 단계에서 추가한다.

- [ ] **Step 2: 실패 정적 테스트 추가**

`Layout.test.mjs`에 추가한다.

```js
test('layout consumes post-login redirect after authentication', () => {
  assert.match(layoutSource, /tennisfolio:postLoginRedirect/);
  assert.match(layoutSource, /sessionStorage\.removeItem\('tennisfolio:postLoginRedirect'\)/);
});
```

- [ ] **Step 3: Layout에 redirect 소비 로직 추가**

`Layout.jsx`에서 로그인 상태가 확인되는 effect 안에 아래 로직을 추가한다. 기존 사용자 조회 effect가 있다면 그 안에서 `isLoggedIn`이 true가 된 직후 한 번만 실행되게 한다.

```js
const redirectAfterLogin = sessionStorage.getItem('tennisfolio:postLoginRedirect');
if (redirectAfterLogin) {
  sessionStorage.removeItem('tennisfolio:postLoginRedirect');
  navigate(redirectAfterLogin, { replace: true });
}
```

- [ ] **Step 4: 통과 확인**

사용자 허용 시 실행:

```powershell
node src/tennisFolio/src/Layout.test.mjs
```

Expected: 테스트 통과.

---

### Task 7: 최종 검증

**Files:**
- All modified files

- [ ] **Step 1: 정적 검색**

```powershell
rg -n "claimCompetition|claimMyCompetition|claimAfterLogin|ownerUserIdSet|내 계정에 저장|이미 다른 계정에 저장된 경기입니다" src/main/java src/test/java src/tennisFolio/src
```

Expected: 백엔드 서비스/컨트롤러/테스트와 프론트 상세 화면/API 테스트가 모두 검색된다.

- [ ] **Step 2: 공백 검증**

```powershell
git diff --check -- src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionDetailResponse.java src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceClaimTest.java src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java src/tennisFolio/src/utils/authApi.js src/tennisFolio/src/page/CompetitionDetail.jsx src/tennisFolio/src/page/CompetitionDetail.css src/tennisFolio/src/page/competitionAccountClaim.test.mjs src/tennisFolio/src/Layout.jsx src/tennisFolio/src/Layout.test.mjs
```

Expected: 출력 없음.

- [ ] **Step 3: 테스트 실행**

사용자가 허용하면 실행한다.

```powershell
.\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceClaimTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest
```

Expected: 모든 테스트 통과.

사용자가 허용하면 실행한다.

```powershell
node src/tennisFolio/src/page/competitionAccountClaim.test.mjs
node src/tennisFolio/src/Layout.test.mjs
```

Expected: 모든 테스트 통과.

- [ ] **Step 4: 수동 시나리오**

로컬 실행 허용을 받은 뒤 브라우저에서 확인한다.

```text
1. 비로그인으로 Competition 생성
2. 생성 직후 상세 화면에서 내 계정에 저장 버튼 확인
3. 버튼 클릭 후 로그인
4. 원래 URL에 claimAfterLogin=1이 붙은 상태로 복귀
5. 저장 성공 메시지 확인
6. URL에서 claimAfterLogin 제거 확인
7. /me/competitions에 해당 Competition 표시 확인
8. 헤더 로그인만 한 경우 자동 저장이 발생하지 않는지 확인
```

---

## Self-Review

- Spec coverage: 단일 owner 정책, 관리자 토큰 검증, `claimAfterLogin=1`, 헤더 로그인 제외, 409 문구, 상세 버튼 노출, 테스트 범위를 모두 태스크에 매핑했다.
- Placeholder scan: 각 단계에 파일, 코드, 명령, 기대 결과를 적었다.
- Type consistency: 백엔드 메서드는 `claimCompetition(String publicId, Long ownerUserId, String adminToken)`, 프론트 API는 `claimMyCompetition(publicId, adminToken)`, 상세 응답 필드는 `ownerUserIdSet`으로 통일했다.
