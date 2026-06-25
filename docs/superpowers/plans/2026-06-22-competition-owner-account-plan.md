# Competition 계정 소유자 연결 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 로그인 사용자가 생성한 Competition을 계정에 자동 연결하고, 로그인 후 내 Competition 목록을 조회할 수 있게 만든다.

**Architecture:** `tb_competition`에 nullable `OWNER_USER_ID`를 추가하고, `POST /api/competitions` 요청에 로그인 인증이 있으면 현재 `userId`를 저장한다. 내 목록 조회는 `GET /api/auth/me/competitions`에서 현재 사용자 id로 `CompetitionQueryService`를 호출해 최신순 요약 목록을 반환한다.

**Tech Stack:** Java 17, Spring Boot, Spring Security, Spring Data JPA, JUnit 5, Mockito, React, React Router, Axios

---

## 구현 전 주의사항

- 이 저장소는 사용자가 명시적으로 요청하지 않는 한 git commit을 만들지 않는다.
- Java/Gradle 빌드와 npm 빌드는 사용자가 명시적으로 요청한 경우에만 실행한다.
- 구현 전 루트 문서 중 한글 markdown은 인코딩이 깨질 수 있다. 깨진 문서는 규칙을 추측하지 말고 현재 소스 코드 기준으로 작업한다.
- `src/main/java/com/tennisfolio/Tennisfolio/matching/ENTITY_DESIGN.md`는 현재 저장소에 없으므로, 발견되지 않으면 기존 matching 코드 구조를 기준으로 진행한다.

## 파일 구조

- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
  - `ownerUserId` 필드를 추가하고 생성자에서 nullable owner를 받을 수 있게 한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/repository/CompetitionRepository.java`
  - 소유자별 최신순 목록 조회 메서드를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionService.java`
  - Competition 생성 시 owner id를 저장하는 오버로드를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
  - `createCompetition(request, ownerUserId)` 흐름을 추가한다.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionSummaryResponse.java`
  - 내 Competition 목록 응답 DTO를 담당한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryService.java`
  - 현재 사용자 소유 Competition 목록 조회 메서드를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/controller/CompetitionController.java`
  - 인증이 있으면 user id를 추출해 생성 서비스에 전달한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
  - `GET /api/auth/me/competitions` 엔드포인트를 추가한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
  - `/api/auth/me/competitions`를 인증 필수 경로에 추가한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/MatchingTestFixtures.java`
  - 테스트에서 owner id가 있는 Competition fixture를 만들 수 있게 한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`
  - 생성 서비스가 owner id를 전달하는지 검증한다.
- Create: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`
  - 소유자별 목록 조회를 JPA 기반으로 검증한다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
  - 내 Competition 목록 API가 현재 user id를 사용하는지 검증한다.
- Modify: `src/tennisFolio/src/utils/authApi.js`
  - `getMyCompetitions()` API 함수를 추가한다.
- Create: `src/tennisFolio/src/page/MyCompetitions.jsx`
  - 로그인 사용자의 Competition 목록 화면을 만든다.
- Create: `src/tennisFolio/src/page/MyCompetitions.css`
  - 목록 화면 스타일을 담당한다.
- Modify: `src/tennisFolio/src/App.jsx`
  - `/me/competitions` 라우트를 추가한다.
- Modify: `src/tennisFolio/src/Layout.jsx`
  - 계정 시트에서 내 Competition 목록으로 이동하는 버튼을 추가한다.
- Modify: `src/tennisFolio/src/Layout.css`
  - 계정 시트 버튼 스타일을 추가한다.

---

### Task 1: Competition 소유자 필드와 저장 흐름 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/entity/Competition.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`

- [ ] **Step 1: 실패하는 서비스 테스트 추가**

`CompetitionCommandServiceTest`에 아래 테스트를 추가한다.

```java
@Test
void createCompetition_passesOwnerUserIdWhenAuthenticated() {
    CompetitionCreateRequest request = new CompetitionCreateRequest(
            "CLUB_SESSION",
            "Club",
            4,
            4,
            2,
            1,
            136L,
            null,
            null
    );
    Competition competition = clubSessionCompetition(1L, "public-id", null);
    ScheduleResult scheduleResult = new ScheduleResult();
    Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

    when(competitionService.createCompetition(request, 1, 136L, 10L)).thenReturn(competition);
    when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
    when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
    when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

    CompetitionCreateResponse response = service.createCompetition(request, 10L);

    assertEquals("public-id", response.getPublicId());
    verify(competitionService).createCompetition(request, 1, 136L, 10L);
}
```

- [ ] **Step 2: 테스트 실패 확인**

사용자가 Java 테스트 실행을 허용한 경우에만 실행한다.

```powershell
rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest
```

Expected: `createCompetition(CompetitionCreateRequest, Long)` 또는 `createCompetition(..., Long)` 오버로드가 없어서 컴파일 실패.

- [ ] **Step 3: `Competition`에 owner 필드 추가**

`Competition.java`에 필드를 추가한다.

```java
@Column(name = "OWNER_USER_ID")
private Long ownerUserId;
```

기존 생성자는 유지하고, owner를 받는 생성자를 추가한다.

```java
public Competition(String name, Integer maleCount, Integer femaleCount,
                  Integer courtCount, Integer rounds, Long seed, CompetitionMode mode) {
    this(name, maleCount, femaleCount, courtCount, rounds, seed, mode, null);
}

public Competition(String name, Integer maleCount, Integer femaleCount,
                  Integer courtCount, Integer rounds, Long seed, CompetitionMode mode, Long ownerUserId) {
    this.name = name;
    this.maleCount = maleCount;
    this.femaleCount = femaleCount;
    this.courtCount = courtCount;
    this.rounds = rounds;
    this.seed = seed;
    this.mode = mode == null ? CompetitionMode.FIXED_SCHEDULE : mode;
    this.ownerUserId = ownerUserId;
}
```

- [ ] **Step 4: `CompetitionService` 생성 오버로드 추가**

기존 메서드는 유지하고 owner id를 받는 오버로드를 추가한다.

```java
public Competition createCompetition(CompetitionCreateRequest request, int rounds, long seed) {
    return createCompetition(request, rounds, seed, null);
}

public Competition createCompetition(
        CompetitionCreateRequest request,
        int rounds,
        long seed,
        Long ownerUserId
) {
    Competition.CompetitionMode mode = resolveMode(request.getMode());
    return competitionRepository.save(new Competition(
            request.getCompetitionName(),
            request.getMaleCount(),
            request.getFemaleCount(),
            request.getCourtCount(),
            rounds,
            seed,
            mode,
            ownerUserId
    ));
}
```

- [ ] **Step 5: `CompetitionCommandService` 생성 오버로드 추가**

기존 공개 메서드는 비로그인 호환용으로 유지한다.

```java
@Transactional
public CompetitionCreateResponse createCompetition(CompetitionCreateRequest request) {
    return createCompetition(request, null);
}

@Transactional
public CompetitionCreateResponse createCompetition(CompetitionCreateRequest request, Long ownerUserId) {
    Competition.CompetitionMode mode = resolveMode(request.getMode());
    validateRequest(request, mode);

    int rounds = mode == Competition.CompetitionMode.CLUB_SESSION
            ? 1
            : calculateRounds(request.getTotalGames(), request.getCourtCount());
    long seed = request.getSeed() != null
            ? request.getSeed()
            : ThreadLocalRandom.current().nextLong(1, 10000);

    Competition competition = competitionService.createCompetition(request, rounds, seed, ownerUserId);

    int scheduleGames = mode == Competition.CompetitionMode.CLUB_SESSION ? rounds : request.getTotalGames();
    ScheduleResult result = generateSchedule(request, mode, scheduleGames, seed);

    Map<String, CompetitionEntry> entriesByPlayerName = competitionEntryCommandService.createCompetitionEntries(competition, request);
    gameService.saveSchedule(competition, result, entriesByPlayerName);
    competitionStatService.createCompetitionStat(competition, result, entriesByPlayerName);

    String competitionAdminToken = competitionAdminTokenService.createToken(competition.getPublicId());
    return CompetitionCreateResponse.from(competition, competitionAdminToken);
}
```

- [ ] **Step 6: 테스트 통과 확인**

사용자가 Java 테스트 실행을 허용한 경우에만 실행한다.

```powershell
rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 2: Competition 생성 컨트롤러에서 로그인 사용자 id 전달

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/controller/CompetitionController.java`

- [ ] **Step 1: 컨트롤러 메서드에 `Authentication` 파라미터 추가**

import를 추가한다.

```java
import org.springframework.security.core.Authentication;
```

`createCompetition` 메서드를 아래처럼 바꾼다.

```java
@PostMapping("/competitions")
public ResponseEntity<ResponseDTO<CompetitionCreateResponse>> createCompetition(
        Authentication authentication,
        @RequestBody CompetitionCreateRequest request
) {
    CompetitionCreateResponse response = competitionCommandService.createCompetition(
            request,
            resolveAuthenticatedUserId(authentication)
    );
    return new ResponseEntity<>(ResponseDTO.success(response), HttpStatus.OK);
}
```

- [ ] **Step 2: anonymous 인증을 걸러내는 helper 추가**

`CompetitionController` 클래스 하단에 private 메서드를 추가한다.

```java
private Long resolveAuthenticatedUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof Long userId) {
        return userId;
    }

    return null;
}
```

Spring Security의 anonymous principal은 `Long`이 아니므로 비로그인 생성은 owner 없이 진행된다.

- [ ] **Step 3: 영향 범위 확인**

사용자가 Java 테스트 실행을 허용한 경우에만 실행한다.

```powershell
rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: 내 Competition 목록 조회 백엔드 추가

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/repository/CompetitionRepository.java`
- Create: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionSummaryResponse.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryService.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/MatchingTestFixtures.java`
- Create: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`

- [ ] **Step 1: repository 조회 메서드 추가**

`CompetitionRepository`에 최신순 조회 메서드를 추가한다.

```java
List<Competition> findByOwnerUserIdOrderByCreateDtDesc(Long ownerUserId);
```

- [ ] **Step 2: 목록 응답 DTO 생성**

`src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionSummaryResponse.java`를 만든다.

```java
package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompetitionSummaryResponse {
    private String publicId;
    private String name;
    private Integer maleCount;
    private Integer femaleCount;
    private Integer courtCount;
    private Integer rounds;
    private String status;
    private String mode;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static CompetitionSummaryResponse from(Competition competition) {
        return new CompetitionSummaryResponse(
                competition.getPublicId(),
                competition.getName(),
                competition.getMaleCount(),
                competition.getFemaleCount(),
                competition.getCourtCount(),
                competition.getRounds(),
                competition.getStatus().name(),
                competition.getMode() == null
                        ? Competition.CompetitionMode.FIXED_SCHEDULE.name()
                        : competition.getMode().name(),
                competition.getCreateDt()
        );
    }
}
```

- [ ] **Step 3: `CompetitionQueryService`에 목록 메서드 추가**

import를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
```

메서드를 추가한다.

```java
@Transactional(readOnly = true)
public List<CompetitionSummaryResponse> getOwnedCompetitions(Long ownerUserId) {
    return competitionRepository.findByOwnerUserIdOrderByCreateDtDesc(ownerUserId)
            .stream()
            .map(CompetitionSummaryResponse::from)
            .toList();
}
```

- [ ] **Step 4: fixture에 owner id 설정 helper 추가**

`MatchingTestFixtures`에 메서드를 추가한다.

```java
public static Competition ownedCompetition(
        Long id,
        String publicId,
        Long ownerUserId,
        Competition.CompetitionMode mode
) {
    Competition competition = competition(id, publicId, null, mode);
    ReflectionTestUtils.setField(competition, "ownerUserId", ownerUserId);
    return competition;
}
```

- [ ] **Step 5: JPA 기반 목록 테스트 생성**

`src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionQueryServiceOwnedCompetitionTest.java`를 만든다.

```java
package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CompetitionQueryService.class)
class CompetitionQueryServiceOwnedCompetitionTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionEntryRepository competitionEntryRepository;

    @Autowired
    private CompetitionStatRepository competitionStatRepository;

    @Autowired
    private GameEntryRepository gameEntryRepository;

    @Autowired
    private CompetitionQueryService competitionQueryService;

    @Test
    void getOwnedCompetitions_returnsOnlyCurrentUsersCompetitionsNewestFirst() {
        Competition firstOwned = competitionRepository.save(new Competition(
                "first",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        competitionRepository.save(new Competition(
                "anonymous",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                null
        ));
        competitionRepository.save(new Competition(
                "other",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                20L
        ));
        Competition secondOwned = competitionRepository.save(new Competition(
                "second",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));

        List<CompetitionSummaryResponse> response =
                competitionQueryService.getOwnedCompetitions(10L);

        assertThat(response)
                .extracting(CompetitionSummaryResponse::getPublicId)
                .containsExactly(secondOwned.getPublicId(), firstOwned.getPublicId());
    }
}
```

필요 없는 repository field가 컴파일 경고를 만들면 제거해도 된다. `CompetitionQueryService` 생성자가 네 repository를 요구하므로 `@Import` 주입이 실패하면 테스트를 `@SpringBootTest`로 바꾸지 말고, 생성자를 직접 호출하는 Mockito 테스트로 축소한다.

- [ ] **Step 6: `AuthController`에 의존성과 엔드포인트 추가**

import를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryService;

import java.util.List;
```

필드와 생성자 파라미터를 추가한다.

```java
private final CompetitionQueryService competitionQueryService;
```

생성자 파라미터 끝에 `CompetitionQueryService competitionQueryService`를 추가하고 필드에 할당한다.

엔드포인트를 추가한다.

```java
@GetMapping("/me/competitions")
public ResponseEntity<ResponseDTO<List<CompetitionSummaryResponse>>> myCompetitions(
        Authentication authentication
) {
    Long userId = (Long) authentication.getPrincipal();
    return ResponseEntity.ok(ResponseDTO.success(
            competitionQueryService.getOwnedCompetitions(userId)
    ));
}
```

- [ ] **Step 7: 보안 설정에 인증 필수 경로 추가**

`SecurityConfig`에서 인증 필수 matcher에 새 경로를 추가한다.

```java
.requestMatchers(
        "/api/auth/me",
        "/api/auth/profile",
        "/api/auth/me/competitions"
).authenticated()
```

- [ ] **Step 8: 컨트롤러 테스트 추가**

`AuthControllerTest`에 mock 필드를 추가한다.

```java
@Mock
CompetitionQueryService competitionQueryService;
```

import를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
```

테스트를 추가한다.

```java
@Test
void myCompetitions_returnsCurrentUsersCompetitions() {
    Authentication authentication =
            new UsernamePasswordAuthenticationToken(1L, null, List.of());
    List<CompetitionSummaryResponse> competitions = List.of(
            new CompetitionSummaryResponse(
                    "public-id",
                    "Club",
                    4,
                    4,
                    2,
                    1,
                    "READY",
                    "CLUB_SESSION",
                    null
            )
    );
    when(competitionQueryService.getOwnedCompetitions(1L)).thenReturn(competitions);

    ResponseEntity<ResponseDTO<List<CompetitionSummaryResponse>>> response =
            authController.myCompetitions(authentication);

    verify(competitionQueryService).getOwnedCompetitions(1L);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).hasSize(1);
    assertThat(response.getBody().getData().get(0).getPublicId()).isEqualTo("public-id");
}
```

- [ ] **Step 9: 백엔드 테스트 확인**

사용자가 Java 테스트 실행을 허용한 경우에만 실행한다.

```powershell
rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: 프론트엔드 내 Competition 목록 화면 추가

**Files:**
- Modify: `src/tennisFolio/src/utils/authApi.js`
- Create: `src/tennisFolio/src/page/MyCompetitions.jsx`
- Create: `src/tennisFolio/src/page/MyCompetitions.css`
- Modify: `src/tennisFolio/src/App.jsx`
- Modify: `src/tennisFolio/src/Layout.jsx`
- Modify: `src/tennisFolio/src/Layout.css`

- [ ] **Step 1: API 함수 추가**

`authApi.js`에 함수를 추가한다.

```javascript
export const getMyCompetitions = () =>
  apiRequestSilent.get('/api/auth/me/competitions');
```

- [ ] **Step 2: 목록 페이지 생성**

`src/tennisFolio/src/page/MyCompetitions.jsx`를 만든다.

```jsx
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyCompetitions } from '../utils/authApi';
import './MyCompetitions.css';

function formatCreatedAt(value) {
  if (!value) {
    return '';
  }

  return value.replace('T', ' ').slice(0, 16);
}

function MyCompetitions() {
  const navigate = useNavigate();
  const [competitions, setCompetitions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let cancelled = false;

    getMyCompetitions()
      .then((response) => {
        if (cancelled) {
          return;
        }
        setCompetitions(response.data.data || []);
        setErrorMessage('');
      })
      .catch((error) => {
        if (cancelled) {
          return;
        }
        if (error.response?.status === 401) {
          setErrorMessage('로그인이 필요합니다.');
        } else {
          setErrorMessage('내 경기 목록을 불러오지 못했습니다.');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="my-competitions-page">
      <header className="my-competitions-header">
        <h1>내 경기</h1>
        <button type="button" onClick={() => navigate('/')}>
          새 경기 만들기
        </button>
      </header>

      {isLoading && (
        <p className="my-competitions-state">불러오는 중입니다.</p>
      )}

      {!isLoading && errorMessage && (
        <p className="my-competitions-state error">{errorMessage}</p>
      )}

      {!isLoading && !errorMessage && competitions.length === 0 && (
        <section className="my-competitions-empty">
          <h2>아직 연결된 경기가 없습니다.</h2>
          <p>로그인한 상태에서 경기를 만들면 이곳에 표시됩니다.</p>
        </section>
      )}

      {!isLoading && !errorMessage && competitions.length > 0 && (
        <div className="my-competitions-list">
          {competitions.map((competition) => (
            <button
              type="button"
              className="my-competition-item"
              key={competition.publicId}
              onClick={() => navigate(`/competitions/${competition.publicId}`)}
            >
              <span className="my-competition-name">{competition.name}</span>
              <span className="my-competition-meta">
                {competition.mode} · {competition.status}
              </span>
              <span className="my-competition-meta">
                남 {competition.maleCount} · 여 {competition.femaleCount} · 코트 {competition.courtCount}
              </span>
              <span className="my-competition-created">
                {formatCreatedAt(competition.createdAt)}
              </span>
            </button>
          ))}
        </div>
      )}
    </main>
  );
}

export default MyCompetitions;
```

- [ ] **Step 3: 목록 페이지 스타일 추가**

`src/tennisFolio/src/page/MyCompetitions.css`를 만든다.

```css
.my-competitions-page {
  width: min(480px, 100%);
  margin: 0 auto;
  padding: 18px 14px 32px;
}

.my-competitions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.my-competitions-header h1 {
  margin: 0;
  color: #101828;
  font-size: 24px;
  font-weight: 900;
}

.my-competitions-header button {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #101828;
  border-radius: 8px;
  background: #101828;
  color: #ffffff;
  font-size: 13px;
  font-weight: 900;
}

.my-competitions-state,
.my-competitions-empty {
  padding: 18px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
  color: #475467;
}

.my-competitions-state.error {
  border-color: #fecdca;
  color: #b42318;
  background: #fffbfa;
}

.my-competitions-empty h2 {
  margin: 0 0 6px;
  color: #101828;
  font-size: 18px;
  font-weight: 900;
}

.my-competitions-empty p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.my-competitions-list {
  display: grid;
  gap: 10px;
}

.my-competition-item {
  display: grid;
  width: 100%;
  gap: 6px;
  padding: 14px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
  text-align: left;
}

.my-competition-name {
  overflow: hidden;
  color: #101828;
  font-size: 16px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-competition-meta,
.my-competition-created {
  color: #667085;
  font-size: 13px;
  font-weight: 700;
}
```

- [ ] **Step 4: 라우트 추가**

`App.jsx`에 import를 추가한다.

```javascript
import MyCompetitions from './page/MyCompetitions.jsx';
```

`Routes`에 라우트를 추가한다.

```jsx
<Route path="/me/competitions" element={<MyCompetitions />} />
```

- [ ] **Step 5: 계정 시트에 이동 버튼 추가**

`Layout.jsx`의 `account-sheet-content` 안에서 이메일 박스와 로그아웃 버튼 사이에 버튼을 추가한다.

```jsx
<button
  type="button"
  className="account-my-competitions-button"
  onClick={() => {
    setSheetMode(null);
    navigate('/me/competitions');
  }}
>
  내 경기 보기
</button>
```

- [ ] **Step 6: 계정 시트 버튼 스타일 추가**

`Layout.css`에 추가한다.

```css
.account-my-competitions-button {
  width: 100%;
  height: 44px;
  border: 1px solid #101828;
  border-radius: 6px;
  background: #101828;
  color: #ffffff;
  font-size: 15px;
  font-weight: 900;
}
```

- [ ] **Step 7: 프론트 정적 검증**

사용자가 npm 검증을 허용한 경우에만 실행한다.

```powershell
rtk npm --prefix src/tennisFolio run lint
```

Expected: lint 에러 없음.

---

### Task 5: 최종 확인

**Files:**
- Review: `docs/superpowers/specs/2026-06-22-competition-owner-account-design.md`
- Review: 변경된 백엔드/프론트 파일 전체

- [ ] **Step 1: spec 요구사항 매핑 확인**

다음 항목을 구현 결과와 대조한다.

```text
비로그인 생성 허용 -> CompetitionController가 null owner를 전달한다.
로그인 생성 자동 연결 -> Authentication principal Long 값을 ownerUserId로 저장한다.
내 목록 API -> GET /api/auth/me/competitions가 ownerUserId 기준으로 반환한다.
기존 관리자 토큰 흐름 유지 -> CompetitionCreateResponse와 edit authorization 로직을 변경하지 않는다.
비로그인 Competition 가져오기 제외 -> claim API를 만들지 않는다.
```

- [ ] **Step 2: 변경 파일 diff 확인**

```powershell
rtk git diff -- src/main/java/com/tennisfolio/Tennisfolio/matching src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java src/test/java/com/tennisfolio/Tennisfolio src/tennisFolio/src
```

Expected: 설계 범위 밖의 리팩터링이나 unrelated 변경이 없다.

- [ ] **Step 3: 사용자가 허용한 경우 전체 관련 테스트 실행**

```powershell
rtk .\gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest --tests com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryServiceOwnedCompetitionTest --tests com.tennisfolio.Tennisfolio.user.api.AuthControllerTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: 사용자가 허용한 경우 프론트 lint 실행**

```powershell
rtk npm --prefix src/tennisFolio run lint
```

Expected: lint 에러 없음.

- [ ] **Step 5: 커밋 여부 확인**

이 저장소의 `AGENTS.md` 규칙에 따라 자동 커밋하지 않는다. 사용자가 명시적으로 커밋을 요청할 때만 staging과 commit을 진행한다.
