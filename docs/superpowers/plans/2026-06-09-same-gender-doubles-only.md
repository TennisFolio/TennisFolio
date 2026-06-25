# 남복/여복 전용 대진 생성 구현 계획

> **작업 에이전트 필수 지침:** 이 계획을 구현할 때는 `superpowers:subagent-driven-development`(권장) 또는 `superpowers:executing-plans`를 사용한다. 각 단계는 체크박스(`- [ ]`)로 진행 상태를 추적한다.

**목표:** `FIXED_SCHEDULE` competition 생성 시 `sameGenderDoublesOnly` 옵션이 켜져 있으면 `MALE`, `FEMALE` 경기만 생성한다.

**구조:** API 요청은 boolean 토글을 받지만, 매칭 엔진 내부에는 허용 `MatchType` 집합을 전달한다. 기존 기본 생성 경로는 random 타입 자동 허용/fallback 동작을 유지하고, 남복/여복 전용 경로는 `MALE`, `FEMALE`만 전달해서 혼복과 random 타입으로 우회하지 않게 한다.

**기술 스택:** Java 17, Spring Boot, JUnit 5, Mockito, Gradle, Jackson.

---

## 파일 구조

- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequest.java`
  - `sameGenderDoublesOnly` 필드를 추가한다.
  - 기존 테스트와 호출부 호환을 위해 현재 생성자 시그니처를 overload로 유지한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/engine/CandidateGenerator.java`
  - 허용 타입 기반 후보 생성을 추가한다.
  - 기존 `allowRandom` 기반 메서드는 유지하고 새 허용 타입 경로로 위임한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/TennisMatchScheduler.java`
  - `Set<MatchType> allowedMatchTypes`를 받는 `generateSchedule` overload를 추가한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`
  - 허용 타입을 받는 overload를 추가한다.
  - 기존 기본 호출의 동작은 유지한다.
  - 제한된 허용 타입이 전달된 경우 random fallback을 실행하지 않는다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
  - 남복/여복 전용 최소 인원 조건을 검증한다.
  - `FIXED_SCHEDULE + sameGenderDoublesOnly=true`인 경우에만 제한된 스케줄러 overload를 호출한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequestTest.java`
  - JSON true 값과 필드 누락 시 false 기본값을 검증한다.
- 생성: `src/test/java/com/tennisfolio/Tennisfolio/matching/engine/CandidateGeneratorTest.java`
  - 후보 생성 경계에서 허용 타입 필터링을 검증한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`
  - 남복/여복 전용 스케줄러 동작과 fallback 금지를 검증한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`
  - command service의 라우팅과 검증을 확인한다.

## 작업 1: DTO 토글 필드 추가

**파일:**
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequestTest.java`
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequest.java`

- [ ] **1단계: 실패하는 DTO 테스트 작성**

`CompetitionCreateRequestTest.java`에 import를 추가한다.

```java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

`CompetitionCreateRequestTest.java`에 테스트를 추가한다.

```java
    @Test
    void deserializesSameGenderDoublesOnly() throws Exception {
        String json = """
                {
                  "mode": "FIXED_SCHEDULE",
                  "competitionName": "Fixed",
                  "maleCount": 8,
                  "femaleCount": 8,
                  "courtCount": 2,
                  "totalGames": 12,
                  "seed": 136,
                  "sameGenderDoublesOnly": true,
                  "malePlayerNames": ["M1", "M2"],
                  "femalePlayerNames": ["F1", "F2"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertTrue(request.isSameGenderDoublesOnly());
    }

    @Test
    void defaultsSameGenderDoublesOnlyToFalseWhenMissing() throws Exception {
        String json = """
                {
                  "mode": "FIXED_SCHEDULE",
                  "competitionName": "Fixed",
                  "maleCount": 8,
                  "femaleCount": 8,
                  "courtCount": 2,
                  "totalGames": 12,
                  "seed": 136,
                  "malePlayerNames": ["M1", "M2"],
                  "femalePlayerNames": ["F1", "F2"]
                }
                """;

        CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

        assertFalse(request.isSameGenderDoublesOnly());
    }
```

- [ ] **2단계: DTO 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequestTest"
```

예상 결과: `CompetitionCreateRequest`에 `isSameGenderDoublesOnly()`가 없어서 실패한다.

- [ ] **3단계: DTO 필드와 생성자 구현**

`CompetitionCreateRequest.java`를 다음처럼 변경한다.

```java
package com.tennisfolio.Tennisfolio.matching.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CompetitionCreateRequest {
    private final String mode;
    private final String competitionName;
    private final int maleCount;
    private final int femaleCount;
    private final int courtCount;
    private final int totalGames;
    private final Long seed;
    private final List<String> malePlayerNames;
    private final List<String> femalePlayerNames;
    private final boolean sameGenderDoublesOnly;

    @JsonCreator
    public CompetitionCreateRequest(
            @JsonProperty("mode") String mode,
            @JsonProperty("competitionName") String competitionName,
            @JsonProperty("maleCount") int maleCount,
            @JsonProperty("femaleCount") int femaleCount,
            @JsonProperty("courtCount") int courtCount,
            @JsonProperty("totalGames") int totalGames,
            @JsonProperty("seed") Long seed,
            @JsonProperty("malePlayerNames") List<String> malePlayerNames,
            @JsonProperty("femalePlayerNames") List<String> femalePlayerNames,
            @JsonProperty("sameGenderDoublesOnly") boolean sameGenderDoublesOnly
    ) {
        this.mode = mode;
        this.competitionName = competitionName;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.courtCount = courtCount;
        this.totalGames = totalGames;
        this.seed = seed;
        this.malePlayerNames = malePlayerNames;
        this.femalePlayerNames = femalePlayerNames;
        this.sameGenderDoublesOnly = sameGenderDoublesOnly;
    }

    public CompetitionCreateRequest(
            String mode,
            String competitionName,
            int maleCount,
            int femaleCount,
            int courtCount,
            int totalGames,
            Long seed,
            List<String> malePlayerNames,
            List<String> femalePlayerNames
    ) {
        this(
                mode,
                competitionName,
                maleCount,
                femaleCount,
                courtCount,
                totalGames,
                seed,
                malePlayerNames,
                femalePlayerNames,
                false
        );
    }
}
```

- [ ] **4단계: DTO 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequestTest"
```

예상 결과: PASS.

## 작업 2: CandidateGenerator 허용 타입 필터 추가

**파일:**
- 생성: `src/test/java/com/tennisfolio/Tennisfolio/matching/engine/CandidateGeneratorTest.java`
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/engine/CandidateGenerator.java`

- [ ] **1단계: 실패하는 후보 생성 테스트 작성**

`CandidateGeneratorTest.java`를 생성한다.

```java
package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateGeneratorTest {

    private final CandidateGenerator generator = new CandidateGenerator();

    @Test
    void generateWithAllowedTypesCreatesOnlyMaleAndFemaleCandidates() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"), male("M3"), male("M4"),
                female("F1"), female("F2"), female("F3"), female("F4")
        );

        List<MatchCandidate> candidates = generator.generate(
                players,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        Set<MatchType> types = candidates.stream()
                .map(candidate -> candidate.type)
                .collect(Collectors.toSet());

        assertEquals(Set.of(MatchType.MALE, MatchType.FEMALE), types);
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.MIXED));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.RANDOM_M3F1));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.RANDOM_M1F3));
    }

    @Test
    void generateWithAllowedTypesDoesNotFallbackToMixedOrRandom() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"),
                female("F1"), female("F2")
        );

        List<MatchCandidate> candidates = generator.generate(
                players,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertTrue(candidates.isEmpty());
    }

    @Test
    void existingAllowRandomFalsePathKeepsNormalTypes() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"), male("M3"), male("M4"),
                female("F1"), female("F2"), female("F3"), female("F4")
        );

        List<MatchCandidate> candidates = generator.generate(players, false);

        Set<MatchType> types = candidates.stream()
                .map(candidate -> candidate.type)
                .collect(Collectors.toSet());

        assertEquals(Set.of(MatchType.MIXED, MatchType.MALE, MatchType.FEMALE), types);
    }

    private GamePlayer male(String id) {
        return new GamePlayer(id, GamePlayer.Gender.MALE);
    }

    private GamePlayer female(String id) {
        return new GamePlayer(id, GamePlayer.Gender.FEMALE);
    }
}
```

- [ ] **2단계: 후보 생성 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.engine.CandidateGeneratorTest"
```

예상 결과: `CandidateGenerator.generate(List<GamePlayer>, Set<MatchType>)`가 없어서 실패한다.

- [ ] **3단계: 허용 타입 기반 후보 생성 구현**

`CandidateGenerator.java`에 다음 변경을 적용한다.

```java
private static final EnumSet<MatchType> NORMAL_TYPES = EnumSet.of(
        MatchType.MIXED,
        MatchType.MALE,
        MatchType.FEMALE
);

private static final EnumSet<MatchType> RANDOM_TYPES = EnumSet.of(
        MatchType.RANDOM_M3F1,
        MatchType.RANDOM_M1F3
);
```

새 메서드를 추가한다.

```java
public List<MatchCandidate> generate(List<GamePlayer> players, Set<MatchType> allowedMatchTypes) {
    List<MatchCandidate> result = new ArrayList<>();
    forEachCandidate(players, allowedMatchTypes, result::add);
    return result;
}

public void forEachCandidate(
        List<GamePlayer> players,
        Set<MatchType> allowedMatchTypes,
        Consumer<MatchCandidate> consumer
) {
    List<GamePlayer> men = new ArrayList<>();
    List<GamePlayer> women = new ArrayList<>();

    for (GamePlayer player : players) {
        if (player.gender == GamePlayer.Gender.MALE) {
            men.add(player);
        } else {
            women.add(player);
        }
    }

    if (allowedMatchTypes.contains(MatchType.MIXED)) {
        generateMixed(men, women, consumer);
    }
    if (allowedMatchTypes.contains(MatchType.MALE)) {
        generateMale(men, consumer);
    }
    if (allowedMatchTypes.contains(MatchType.FEMALE)) {
        generateFemale(women, consumer);
    }
    if (allowedMatchTypes.contains(MatchType.RANDOM_M3F1)) {
        generateRandomM3F1(men, women, consumer);
    }
    if (allowedMatchTypes.contains(MatchType.RANDOM_M1F3)) {
        generateRandomM1F3(men, women, consumer);
    }
}
```

기존 `forEachCandidate(List<GamePlayer>, boolean, Consumer<MatchCandidate>)`는 다음처럼 새 경로로 위임한다.

```java
public void forEachCandidate(List<GamePlayer> players, boolean allowRandom, Consumer<MatchCandidate> consumer) {
    EnumSet<MatchType> allowedMatchTypes = EnumSet.copyOf(NORMAL_TYPES);
    if (allowRandom) {
        allowedMatchTypes.addAll(RANDOM_TYPES);
    }
    forEachCandidate(players, allowedMatchTypes, consumer);
}
```

- [ ] **4단계: 후보 생성 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.engine.CandidateGeneratorTest"
```

예상 결과: PASS.

## 작업 3: 스케줄러 허용 타입 경로 추가

**파일:**
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/TennisMatchScheduler.java`
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`

- [ ] **1단계: 실패하는 스케줄러 테스트 작성**

`TennisMatchSchedulerTest.java`에 테스트를 추가한다.

```java
    @Test
    void generateSchedule_withSameGenderOnlyAllowedTypesCreatesOnlyMaleAndFemaleMatches() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult result = scheduler.generateSchedule(
                8,
                8,
                2,
                8,
                136,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertEquals(8, result.matches.size());
        assertTrue(result.matches.stream()
                .allMatch(match -> match.type == MatchType.MALE || match.type == MatchType.FEMALE));
    }

    @Test
    void generateSchedule_withSameGenderOnlyAllowedTypesDoesNotFallbackToMixedOrRandom() {
        TennisMatchScheduler scheduler = createScheduler();

        assertThrows(
                NoSuchElementException.class,
                () -> scheduler.generateSchedule(
                        2,
                        2,
                        1,
                        1,
                        136,
                        EnumSet.of(MatchType.MALE, MatchType.FEMALE)
                )
        );
    }

    @Test
    void generateSchedule_withOnlyMenProducesMaleMatchesInDefaultAndSameGenderOnlyPolicies() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult defaultResult = scheduler.generateSchedule(8, 0, 2, 4, 136);
        ScheduleResult sameGenderOnlyResult = scheduler.generateSchedule(
                8,
                0,
                2,
                4,
                136,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertTrue(defaultResult.matches.stream().allMatch(match -> match.type == MatchType.MALE));
        assertTrue(sameGenderOnlyResult.matches.stream().allMatch(match -> match.type == MatchType.MALE));
    }
```

- [ ] **2단계: 스케줄러 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest"
```

예상 결과: `TennisMatchScheduler.generateSchedule(..., Set<MatchType>)`가 없어서 실패한다.

- [ ] **3단계: TennisMatchScheduler overload 구현**

`TennisMatchScheduler.java`에 import와 overload를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;

import java.util.Set;
```

```java
public ScheduleResult generateSchedule(
        int male,
        int female,
        int court,
        int totalGames,
        long seed,
        Set<MatchType> allowedMatchTypes
) {
    return fixedScheduleGenerator.generateSchedule(male, female, court, totalGames, seed, allowedMatchTypes);
}
```

- [ ] **4단계: FixedScheduleGenerator overload 구현**

`FixedScheduleGenerator.java`에 import를 추가한다.

```java
import java.util.EnumSet;
```

필드 근처에 기본 타입 상수를 추가한다.

```java
private static final EnumSet<MatchType> NORMAL_TYPES = EnumSet.of(
        MatchType.MIXED,
        MatchType.MALE,
        MatchType.FEMALE
);
```

기존 public `generateSchedule`은 기본 정책을 구성하도록 변경하고, 허용 타입을 받는 public overload를 추가한다.

```java
public ScheduleResult generateSchedule(int male, int female, int court, int totalGames, long seed) {
    int rounds = calculateRounds(totalGames, court);
    boolean allowRandom = shouldAllowRandomType(male, female, court, totalGames);
    EnumSet<MatchType> allowedMatchTypes = EnumSet.copyOf(NORMAL_TYPES);
    if (allowRandom) {
        allowedMatchTypes.add(MatchType.RANDOM_M3F1);
        allowedMatchTypes.add(MatchType.RANDOM_M1F3);
    }

    try {
        return generateSchedule(male, female, court, totalGames, rounds, seed, allowedMatchTypes);
    } catch (NoSuchElementException e) {
        if (allowRandom || !canScheduleRandomType(male, female)) {
            throw e;
        }

        EnumSet<MatchType> fallbackAllowedTypes = EnumSet.copyOf(NORMAL_TYPES);
        fallbackAllowedTypes.add(MatchType.RANDOM_M3F1);
        fallbackAllowedTypes.add(MatchType.RANDOM_M1F3);
        return generateSchedule(male, female, court, totalGames, rounds, seed, fallbackAllowedTypes);
    }
}

public ScheduleResult generateSchedule(
        int male,
        int female,
        int court,
        int totalGames,
        long seed,
        Set<MatchType> allowedMatchTypes
) {
    int rounds = calculateRounds(totalGames, court);
    return generateSchedule(male, female, court, totalGames, rounds, seed, allowedMatchTypes);
}
```

private `generateSchedule`과 `selectBestCandidate`의 `boolean allowRandom` 파라미터를 `Set<MatchType> allowedMatchTypes`로 바꾸고, 후보 생성 호출을 다음처럼 바꾼다.

```java
generator.forEachCandidate(availablePlayers, allowedMatchTypes, candidate -> {
```

- [ ] **5단계: 스케줄러 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest"
```

예상 결과: PASS.

## 작업 4: Command Service 라우팅과 검증

**파일:**
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`

- [ ] **1단계: 실패하는 command service 테스트 작성**

`CompetitionCommandServiceTest.java`에 import를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;

import java.util.EnumSet;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
```

테스트를 추가한다.

```java
    @Test
    void createCompetition_passesSameGenderOnlyPolicyForFixedSchedule() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                8,
                2,
                8,
                136L,
                null,
                null,
                true
        );
        Competition competition = new Competition("Fixed", 8, 8, 2, 4, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 4, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(
                eq(8),
                eq(8),
                eq(2),
                eq(8),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        )).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(
                8,
                8,
                2,
                8,
                136L,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );
        verify(scheduler, never()).generateSchedule(8, 8, 2, 8, 136L);
    }

    @Test
    void createCompetition_ignoresSameGenderOnlyForClubSession() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "CLUB_SESSION",
                "Club",
                4,
                4,
                2,
                1,
                136L,
                null,
                null,
                true
        );
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 1, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(4, 4, 2, 1, 136L);
        verify(scheduler, never()).generateSchedule(
                eq(4),
                eq(4),
                eq(2),
                eq(1),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        );
    }

    @Test
    void createCompetition_rejectsSameGenderOnlyWhenIncludedMaleCountIsLessThanFour() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                3,
                3,
                1,
                1,
                136L,
                null,
                null,
                true
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createCompetition(request)
        );

        assertEquals(
                "sameGenderDoublesOnly requires each included gender to have at least 4 players",
                exception.getMessage()
        );
    }

    @Test
    void createCompetition_rejectsSameGenderOnlyWhenIncludedFemaleCountIsLessThanFour() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                3,
                2,
                4,
                136L,
                null,
                null,
                true
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createCompetition(request)
        );

        assertEquals(
                "sameGenderDoublesOnly requires each included gender to have at least 4 players",
                exception.getMessage()
        );
    }

    @Test
    void createCompetition_allowsSameGenderOnlyWhenFemaleCountIsZero() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                0,
                2,
                4,
                136L,
                null,
                null,
                true
        );
        Competition competition = new Competition("Fixed", 8, 0, 2, 2, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 2, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(
                eq(8),
                eq(0),
                eq(2),
                eq(4),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        )).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(
                8,
                0,
                2,
                4,
                136L,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );
    }
```

- [ ] **2단계: command service 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest"
```

예상 결과: `CompetitionCommandService`가 아직 기본 스케줄러 메서드만 호출하고, 남복/여복 전용 최소 조건을 검증하지 않아서 실패한다.

- [ ] **3단계: 라우팅과 검증 구현**

`CompetitionCommandService.java`에 import를 추가한다.

```java
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;

import java.util.EnumSet;
```

상수를 추가한다.

```java
private static final EnumSet<MatchType> SAME_GENDER_DOUBLES_TYPES = EnumSet.of(
        MatchType.MALE,
        MatchType.FEMALE
);
```

기존 스케줄러 호출을 다음 구조로 바꾼다.

```java
int scheduleGames = mode == Competition.CompetitionMode.CLUB_SESSION ? rounds : request.getTotalGames();
ScheduleResult result = generateSchedule(request, mode, scheduleGames, seed);
```

private 메서드를 추가한다.

```java
private ScheduleResult generateSchedule(
        CompetitionCreateRequest request,
        Competition.CompetitionMode mode,
        int scheduleGames,
        long seed
) {
    if (mode == Competition.CompetitionMode.FIXED_SCHEDULE && request.isSameGenderDoublesOnly()) {
        return scheduler.generateSchedule(
                request.getMaleCount(),
                request.getFemaleCount(),
                request.getCourtCount(),
                scheduleGames,
                seed,
                SAME_GENDER_DOUBLES_TYPES
        );
    }

    return scheduler.generateSchedule(
            request.getMaleCount(),
            request.getFemaleCount(),
            request.getCourtCount(),
            scheduleGames,
            seed
    );
}
```

`validateRequest`의 player count 검증 뒤에 남복/여복 전용 최소 조건을 추가한다.

```java
if (mode == Competition.CompetitionMode.FIXED_SCHEDULE
        && request.isSameGenderDoublesOnly()
        && isIncludedGenderBelowDoublesMinimum(request)) {
    throw new IllegalArgumentException(
            "sameGenderDoublesOnly requires each included gender to have at least 4 players"
    );
}
```

private 메서드를 추가한다.

```java
private boolean isIncludedGenderBelowDoublesMinimum(CompetitionCreateRequest request) {
    return isIncludedButBelowDoublesMinimum(request.getMaleCount())
            || isIncludedButBelowDoublesMinimum(request.getFemaleCount());
}

private boolean isIncludedButBelowDoublesMinimum(int playerCount) {
    return playerCount > 0 && playerCount < 4;
}
```

- [ ] **4단계: command service 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest"
```

예상 결과: PASS.

## 작업 5: 전체 검증

**파일:**
- 코드 변경 없음.

- [ ] **1단계: Java 컴파일**

실행:

```powershell
.\gradlew.bat compileJava
```

예상 결과: PASS.

- [ ] **2단계: 테스트 컴파일**

실행:

```powershell
.\gradlew.bat compileTestJava
```

예상 결과: PASS.

- [ ] **3단계: matching 관련 테스트 실행**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.*" --tests "com.tennisfolio.Tennisfolio.matching.service.*" --tests "com.tennisfolio.Tennisfolio.matching.dto.*" --tests "com.tennisfolio.Tennisfolio.matching.engine.*"
```

예상 결과: PASS.

- [ ] **4단계: diff 확인**

실행:

```powershell
git diff -- src/main/java/com/tennisfolio/Tennisfolio/matching src/test/java/com/tennisfolio/Tennisfolio/matching
```

확인할 내용:

- DTO에는 새 요청 필드와 호환 생성자만 추가되어 있다.
- 후보 생성은 허용 타입 기준으로 필터링된다.
- scheduler/fixed generator의 기본 메서드 경로는 유지된다.
- `Competition`에는 새 필드가 저장되지 않는다.
- `CLUB_SESSION` 다음 게임 생성 로직은 변경되지 않았다.

## 자체 검토

설계 반영 범위:

- API에서 `sameGenderDoublesOnly`를 받는다: 작업 1.
- 필드 누락 시 false로 처리한다: 작업 1.
- `FIXED_SCHEDULE`에만 적용한다: 작업 4.
- `CLUB_SESSION`은 유지한다: 작업 4, 작업 5 diff 확인.
- `MALE`, `FEMALE`만 허용한다: 작업 2, 작업 3.
- 제한 모드에서 mixed/random fallback을 하지 않는다: 작업 2, 작업 3.
- 기존 공정성 점수는 유지한다: 작업 3에서 후보 타입만 제한하고 `ScoreCalculator`는 변경하지 않는다.
- DB/entity 저장은 하지 않는다: 작업 4, 작업 5 diff 확인.
- 검증 명령을 포함한다: 작업 5.

완료 확인: 미해결 표시나 열린 구현 메모가 남아 있지 않다.

타입 일관성: 새 public 경계는 `Set<MatchType>`를 사용하고, 남복/여복 전용 정책은 `EnumSet.of(MatchType.MALE, MatchType.FEMALE)`로 표현한다.

---

## 추가 작업: 남복/여복 전용 생성 보장 개선

**배경:** 현재 남복/여복 전용 경로는 `MALE`, `FEMALE` 후보만 사용하지만, `FixedScheduleGenerator`가 greedy 방식으로 한 경기씩 선택한다. 남자 6명, 여자 5명, 코트 2개, 총 11경기처럼 산술적으로 가능한 요청도 앞선 선택이 미래 후보를 막으면 `No valid match candidate`로 실패할 수 있다.

**목표:** 남복/여복 전용으로 산술적으로 가능한 `FIXED_SCHEDULE` 요청은 greedy 선택 실패 때문에 생성 실패하지 않게 한다. 혼복과 `RANDOM_*` fallback은 계속 금지한다.

**아키텍처:** 남복/여복 전용 경로에만 성별별 경기 수 목표 계산과 backtracking 생성을 추가한다. 기본 생성 경로는 기존 greedy + random fallback 정책을 유지한다.

### 추가 파일 구조

- 생성: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTarget.java`
  - 남복/여복 전용 생성에서 사용할 목표 경기 수와 선수별 출전 목표 범위를 담는다.
- 생성: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTargetCalculator.java`
  - `maleCount`, `femaleCount`, `totalGames`로 가능한 목표 후보를 계산한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`
  - `allowedMatchTypes == {MALE, FEMALE}`인 경우 전용 backtracking 경로를 사용한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`
  - 실패 재현과 생성 보장 회귀 테스트를 추가한다.
- 생성: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTargetCalculatorTest.java`
  - 성별별 경기 수 목표 계산을 단위 테스트한다.

## 작업 6: 성별별 경기 수 목표 계산 추가

**파일:**
- 생성: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTargetCalculatorTest.java`
- 생성: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTarget.java`
- 생성: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/SameGenderScheduleTargetCalculator.java`

- [ ] **1단계: 실패하는 목표 계산 테스트 작성**

`SameGenderScheduleTargetCalculatorTest.java`를 생성한다.

```java
package com.tennisfolio.Tennisfolio.matching.service.fixed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SameGenderScheduleTargetCalculatorTest {

    private final SameGenderScheduleTargetCalculator calculator = new SameGenderScheduleTargetCalculator();

    @Test
    void calculatesExactFourGamesTargetForSixMenFiveWomenElevenGames() {
        List<SameGenderScheduleTarget> targets = calculator.calculate(6, 5, 11);

        assertFalse(targets.isEmpty());
        SameGenderScheduleTarget target = targets.get(0);
        assertEquals(6, target.maleGames());
        assertEquals(5, target.femaleGames());
        assertEquals(4, target.minGamesPerPlayer());
        assertEquals(4, target.maxGamesPerPlayer());
    }

    @Test
    void calculatesRangeTargetWithoutForcingEveryoneToFourGames() {
        List<SameGenderScheduleTarget> targets = calculator.calculate(6, 5, 10);

        assertFalse(targets.isEmpty());
        SameGenderScheduleTarget target = targets.get(0);
        assertEquals(5, target.maleGames());
        assertEquals(5, target.femaleGames());
        assertEquals(3, target.minGamesPerPlayer());
        assertEquals(4, target.maxGamesPerPlayer());
    }
}
```

- [ ] **2단계: 목표 계산 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.fixed.SameGenderScheduleTargetCalculatorTest"
```

예상 결과: `SameGenderScheduleTargetCalculator`와 `SameGenderScheduleTarget`가 없어서 컴파일 실패.

- [ ] **3단계: 목표 record 추가**

`SameGenderScheduleTarget.java`를 생성한다.

```java
package com.tennisfolio.Tennisfolio.matching.service.fixed;

public record SameGenderScheduleTarget(
        int maleGames,
        int femaleGames,
        int minGamesPerPlayer,
        int maxGamesPerPlayer
) {
}
```

- [ ] **4단계: 목표 계산기 구현**

`SameGenderScheduleTargetCalculator.java`를 생성한다.

```java
package com.tennisfolio.Tennisfolio.matching.service.fixed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SameGenderScheduleTargetCalculator {

    public List<SameGenderScheduleTarget> calculate(int maleCount, int femaleCount, int totalGames) {
        int playerCount = maleCount + femaleCount;
        int totalSlots = totalGames * 4;
        int minGames = totalSlots / playerCount;
        int maxGames = (int) Math.ceil((double) totalSlots / playerCount);
        int extraSlots = totalSlots - playerCount * minGames;

        List<SameGenderScheduleTarget> targets = new ArrayList<>();
        for (int maleExtraSlots = 0; maleExtraSlots <= Math.min(maleCount, extraSlots); maleExtraSlots++) {
            int femaleExtraSlots = extraSlots - maleExtraSlots;
            if (femaleExtraSlots < 0 || femaleExtraSlots > femaleCount) {
                continue;
            }

            int maleSlots = maleCount * minGames + maleExtraSlots;
            int femaleSlots = femaleCount * minGames + femaleExtraSlots;
            if (maleSlots % 4 != 0 || femaleSlots % 4 != 0) {
                continue;
            }

            int maleGames = maleSlots / 4;
            int femaleGames = femaleSlots / 4;
            if (maleGames + femaleGames != totalGames) {
                continue;
            }
            if (maleGames > 0 && maleCount < 4) {
                continue;
            }
            if (femaleGames > 0 && femaleCount < 4) {
                continue;
            }

            targets.add(new SameGenderScheduleTarget(maleGames, femaleGames, minGames, maxGames));
        }

        targets.sort(Comparator
                .comparingInt((SameGenderScheduleTarget target) -> Math.abs(target.maleGames() - target.femaleGames()))
                .thenComparingInt(SameGenderScheduleTarget::maleGames));
        return targets;
    }
}
```

- [ ] **5단계: 목표 계산 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.fixed.SameGenderScheduleTargetCalculatorTest"
```

예상 결과: PASS.

## 작업 7: 남복/여복 전용 실패 재현 테스트 추가

**파일:**
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`

- [ ] **1단계: 실패하는 회귀 테스트 작성**

`TennisMatchSchedulerTest.java`에 테스트를 추가한다.

```java
    @Test
    void generateSchedule_sameGenderOnlyCreatesSixMenFiveWomenElevenGames() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult result = scheduler.generateSchedule(
                6,
                5,
                2,
                11,
                136,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertEquals(11, result.matches.size());
        assertEquals(6, result.matches.stream().filter(match -> match.type == MatchType.MALE).count());
        assertEquals(5, result.matches.stream().filter(match -> match.type == MatchType.FEMALE).count());
        assertTrue(result.matches.stream()
                .allMatch(match -> match.type == MatchType.MALE || match.type == MatchType.FEMALE));
    }

    @Test
    void generateSchedule_sameGenderOnlyDoesNotForceEveryoneToFourGamesWhenRangeIsThreeToFour() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult result = scheduler.generateSchedule(
                6,
                5,
                2,
                10,
                136,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertEquals(10, result.matches.size());
        assertEquals(5, result.matches.stream().filter(match -> match.type == MatchType.MALE).count());
        assertEquals(5, result.matches.stream().filter(match -> match.type == MatchType.FEMALE).count());
        assertTrue(result.matches.stream()
                .allMatch(match -> match.type == MatchType.MALE || match.type == MatchType.FEMALE));
    }
```

- [ ] **2단계: 회귀 테스트 실패 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest.generateSchedule_sameGenderOnlyCreatesSixMenFiveWomenElevenGames" --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest.generateSchedule_sameGenderOnlyDoesNotForceEveryoneToFourGamesWhenRangeIsThreeToFour"
```

예상 결과: 현재 greedy 경로가 특정 입력에서 `No valid match candidate`를 던져 실패한다.

## 작업 8: 남복/여복 전용 backtracking 경로 구현

**파일:**
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`

- [ ] **1단계: 전용 경로 분기 추가**

`FixedScheduleGenerator`에 필드를 추가한다.

```java
private final SameGenderScheduleTargetCalculator sameGenderTargetCalculator = new SameGenderScheduleTargetCalculator();
```

허용 타입 overload 시작 부분에 분기를 추가한다.

```java
public ScheduleResult generateSchedule(
        int male,
        int female,
        int court,
        int totalGames,
        long seed,
        Set<MatchType> allowedMatchTypes
) {
    if (isSameGenderOnly(allowedMatchTypes)) {
        return generateSameGenderOnlySchedule(male, female, court, totalGames, seed);
    }

    int rounds = calculateRounds(totalGames, court);
    return generateSchedule(male, female, court, totalGames, rounds, seed, allowedMatchTypes);
}

private boolean isSameGenderOnly(Set<MatchType> allowedMatchTypes) {
    return allowedMatchTypes.size() == 2
            && allowedMatchTypes.contains(MatchType.MALE)
            && allowedMatchTypes.contains(MatchType.FEMALE);
}
```

- [ ] **2단계: 목표 후보를 순서대로 시도하는 메서드 추가**

`FixedScheduleGenerator`에 메서드를 추가한다.

```java
private ScheduleResult generateSameGenderOnlySchedule(
        int male,
        int female,
        int court,
        int totalGames,
        long seed
) {
    List<SameGenderScheduleTarget> targets = sameGenderTargetCalculator.calculate(male, female, totalGames);
    if (targets.isEmpty()) {
        throw new IllegalArgumentException(
                "sameGenderDoublesOnly cannot allocate same-gender game counts for the requested player distribution"
        );
    }

    NoSuchElementException lastFailure = null;
    for (SameGenderScheduleTarget target : targets) {
        try {
            return generateSameGenderOnlySchedule(male, female, court, totalGames, seed, target);
        } catch (NoSuchElementException e) {
            lastFailure = e;
        }
    }

    if (lastFailure != null) {
        throw lastFailure;
    }
    throw new NoSuchElementException("No valid match candidate");
}
```

- [ ] **3단계: backtracking 생성 메서드 추가**

`FixedScheduleGenerator`에 메서드를 추가한다.

```java
private ScheduleResult generateSameGenderOnlySchedule(
        int male,
        int female,
        int court,
        int totalGames,
        long seed,
        SameGenderScheduleTarget target
) {
    this.random = new Random(seed);
    List<GamePlayer> players = createPlayers(male, female);
    ScheduleResult result = new ScheduleResult();
    Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
    for (MatchType type : MatchType.values()) {
        typeCount.put(type, 0);
    }
    Map<Set<String>, Integer> groupCount = new HashMap<>();
    int rounds = calculateRounds(totalGames, court);

    boolean solved = fillSameGenderOnly(
            players,
            result,
            typeCount,
            groupCount,
            target,
            court,
            totalGames,
            rounds,
            male,
            female,
            0
    );

    if (!solved) {
        throw new NoSuchElementException("No valid match candidate");
    }
    return result;
}
```

- [ ] **4단계: recursive fill 메서드 추가**

`FixedScheduleGenerator`에 메서드를 추가한다.

```java
private boolean fillSameGenderOnly(
        List<GamePlayer> players,
        ScheduleResult result,
        Map<MatchType, Integer> typeCount,
        Map<Set<String>, Integer> groupCount,
        SameGenderScheduleTarget target,
        int court,
        int totalGames,
        int rounds,
        int male,
        int female,
        int gameIndex
) {
    if (gameIndex == totalGames) {
        return typeCount.get(MatchType.MALE) == target.maleGames()
                && typeCount.get(MatchType.FEMALE) == target.femaleGames()
                && players.stream().allMatch(player -> player.totalGames >= target.minGamesPerPlayer()
                        && player.totalGames <= target.maxGamesPerPlayer());
    }

    int round = gameIndex / court + 1;
    int courtNumber = gameIndex % court + 1;
    Set<GamePlayer> usedThisRound = usedPlayersInRound(result, round);
    List<GamePlayer> availablePlayers = players.stream()
            .filter(player -> !usedThisRound.contains(player))
            .toList();

    List<MatchCandidate> candidates = new ArrayList<>();
    generator.forEachCandidate(availablePlayers, EnumSet.of(MatchType.MALE, MatchType.FEMALE), candidate -> {
        if (typeCount.get(candidate.type) >= targetCount(target, candidate.type)) {
            return;
        }
        if (!constraintChecker.isValid(candidate, players, usedThisRound, court, target.maxGamesPerPlayer())) {
            return;
        }
        candidates.add(candidate);
    });

    candidates.sort((left, right) -> Integer.compare(
            scoreSameGenderCandidate(right, typeCount, round, rounds, groupCount, male, female),
            scoreSameGenderCandidate(left, typeCount, round, rounds, groupCount, male, female)
    ));

    for (MatchCandidate candidate : candidates) {
        apply(candidate, groupCount);
        typeCount.put(candidate.type, typeCount.get(candidate.type) + 1);
        result.matches.add(new GameMatch(round, courtNumber, candidate.type, candidate.teamA, candidate.teamB));

        if (fillSameGenderOnly(players, result, typeCount, groupCount, target, court, totalGames, rounds, male, female, gameIndex + 1)) {
            return true;
        }

        result.matches.remove(result.matches.size() - 1);
        typeCount.put(candidate.type, typeCount.get(candidate.type) - 1);
        rollback(candidate, groupCount);
    }

    return false;
}
```

- [ ] **5단계: helper 메서드 추가**

`FixedScheduleGenerator`에 helper를 추가한다.

```java
private Set<GamePlayer> usedPlayersInRound(ScheduleResult result, int round) {
    Set<GamePlayer> used = new HashSet<>();
    for (GameMatch match : result.matches) {
        if (match.round == round) {
            used.addAll(match.teamA);
            used.addAll(match.teamB);
        }
    }
    return used;
}

private int targetCount(SameGenderScheduleTarget target, MatchType type) {
    return switch (type) {
        case MALE -> target.maleGames();
        case FEMALE -> target.femaleGames();
        case MIXED, RANDOM_M3F1, RANDOM_M1F3 -> 0;
    };
}

private int scoreSameGenderCandidate(
        MatchCandidate candidate,
        Map<MatchType, Integer> typeCount,
        int round,
        int rounds,
        Map<Set<String>, Integer> groupCount,
        int male,
        int female
) {
    return scoreCalculator.score(
            candidate,
            typeCount,
            Set.of(),
            round,
            rounds,
            groupCount,
            male,
            female
    );
}
```

- [ ] **6단계: rollback 메서드 추가**

`apply`가 변경한 상태를 반대로 되돌리는 `rollback`을 추가한다.

```java
private void rollback(MatchCandidate c, Map<Set<String>, Integer> groupCount) {
    List<GamePlayer> teamA = c.teamA;
    List<GamePlayer> teamB = c.teamB;
    List<GamePlayer> allPlayers = c.allPlayers();

    for (GamePlayer p : allPlayers) {
        p.totalGames--;
        p.typeExperience.merge(c.type, -1, Integer::sum);
        if (p.typeExperience.getOrDefault(c.type, 0) <= 0) {
            p.typeExperience.remove(c.type);
        }
    }

    for (GamePlayer p1 : teamA) {
        for (GamePlayer p2 : teamA) {
            if (p1 != p2) {
                decrementCount(p1.partnerCount, p2.id);
            }
        }
    }

    for (GamePlayer p1 : teamB) {
        for (GamePlayer p2 : teamB) {
            if (p1 != p2) {
                decrementCount(p1.partnerCount, p2.id);
            }
        }
    }

    for (GamePlayer p1 : teamA) {
        for (GamePlayer p2 : teamB) {
            decrementCount(p1.opponentCount, p2.id);
            decrementCount(p2.opponentCount, p1.id);
        }
    }

    Set<String> group = new HashSet<>(4);
    group.add(allPlayers.get(0).id);
    group.add(allPlayers.get(1).id);
    group.add(allPlayers.get(2).id);
    group.add(allPlayers.get(3).id);
    decrementCount(groupCount, group);
}

private <K> void decrementCount(Map<K, Integer> counts, K key) {
    int next = counts.getOrDefault(key, 0) - 1;
    if (next <= 0) {
        counts.remove(key);
    } else {
        counts.put(key, next);
    }
}
```

- [ ] **7단계: 회귀 테스트 통과 확인**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest.generateSchedule_sameGenderOnlyCreatesSixMenFiveWomenElevenGames" --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest.generateSchedule_sameGenderOnlyDoesNotForceEveryoneToFourGamesWhenRangeIsThreeToFour"
```

예상 결과: PASS.

## 작업 9: 전체 same-gender 테스트와 컴파일 검증

**파일:**
- 코드 변경 없음.

- [ ] **1단계: same-gender 관련 테스트 실행**

실행:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest" --tests "com.tennisfolio.Tennisfolio.matching.service.fixed.SameGenderScheduleTargetCalculatorTest" --tests "com.tennisfolio.Tennisfolio.matching.engine.CandidateGeneratorTest" --tests "com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest"
```

예상 결과: PASS.

- [ ] **2단계: 컴파일 확인**

실행:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat compileTestJava
```

예상 결과: 둘 다 PASS.

- [ ] **3단계: diff 확인**

실행:

```powershell
git diff -- src/main/java/com/tennisfolio/Tennisfolio/matching src/test/java/com/tennisfolio/Tennisfolio/matching
```

확인할 내용:

- 기본 스케줄 생성 경로의 random fallback 정책은 유지된다.
- 남복/여복 전용 경로에서는 `MIXED`, `RANDOM_M3F1`, `RANDOM_M1F3` 후보를 만들지 않는다.
- 남복/여복 전용 경로는 성별별 목표 계산을 먼저 수행한다.
- 남자 6명, 여자 5명, 총 11경기 케이스가 더 이상 `No valid match candidate`로 실패하지 않는다.
- 남자 6명, 여자 5명, 총 10경기 케이스에서 모든 선수를 4경기로 강제하지 않는다.

## 추가 자체 검토

- spec의 "산술적으로 가능한 남복/여복 전용 요청은 greedy 실패 때문에 생성 실패하지 않는다" 요구는 작업 7과 작업 8에서 다룬다.
- spec의 "모든 선수를 4경기로 강제하지 않는다" 요구는 작업 6과 작업 7에서 다룬다.
- spec의 "혼복이나 random fallback으로 우회하지 않는다" 요구는 작업 8의 `EnumSet.of(MatchType.MALE, MatchType.FEMALE)` 후보 생성으로 유지한다.
- 기본 정책 변경 금지는 작업 8의 `isSameGenderOnly` 분기로 보장한다.
