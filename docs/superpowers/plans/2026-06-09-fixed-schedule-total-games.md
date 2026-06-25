# 고정 스케줄 총 게임 수 입력 구현 계획

> **Agent 작업자 필수 안내:** 이 계획을 태스크 단위로 구현할 때는 `superpowers:subagent-driven-development` 권장 또는 `superpowers:executing-plans`를 사용한다. 단계 추적을 위해 checkbox(`- [ ]`) 문법을 사용한다.

**목표:** `FIXED_SCHEDULE` competition 생성에서 `hours` 대신 `totalGames`를 입력받도록 바꾸고, 저장되는 스케줄 그룹 값인 `rounds`는 유지한다.

**아키텍처:** `Competition.rounds`와 `Game` 저장 모델은 변경하지 않는다. `CompetitionCommandService`에서 `totalGames`를 `rounds = ceil(totalGames / courtCount)`로 변환하고, 고정 스케줄 마지막 라운드가 `courtCount`보다 적은 게임을 가질 수 있게 하여 정확히 `totalGames`개 match를 생성한다.

**기술 스택:** Java 17, Spring Boot, JUnit 5, Mockito, Gradle.

---

## 파일 구조

- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequest.java`
  - 요청 필드 `hours`를 `totalGames`로 교체한다.
  - Jackson 생성자 기반 역직렬화 방식은 유지한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
  - `ROUNDS_PER_HOUR`, `MAX_HOURS`를 제거한다.
  - 고정 스케줄 `totalGames` 검증을 추가한다.
  - `totalGames`와 `courtCount`로 `rounds`를 계산한다.
  - 고정 스케줄 생성 시 scheduler에 `totalGames`를 전달한다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/TennisMatchScheduler.java`
  - 고정 스케줄 생성 파라미터 의미를 `rounds`에서 `totalGames`로 바꾼다.
  - `generateNextClubSessionGame`은 변경하지 않는다.
- 수정: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`
  - `totalGames`를 입력받는다.
  - 내부에서 `rounds`를 계산한다.
  - 각 라운드마다 `min(court, remainingGames)`개 게임을 생성한다.
  - 슬롯 수와 배치 가능성 계산에는 `totalGames`를 사용한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequestTest.java`
  - `totalGames` 역직렬화를 검증한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`
  - 파생 `rounds`와 scheduler 호출을 검증한다.
  - 검증 실패 테스트를 추가한다.
- 수정: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`
  - 고정 스케줄 호출을 `rounds` 기준에서 `totalGames` 기준으로 수정한다.
  - 마지막 부분 라운드 테스트를 추가한다.

---

### Task 1: DTO 계약 수정

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequest.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequestTest.java`

- [ ] **Step 1: 실패하는 DTO 역직렬화 테스트 작성**

`CompetitionCreateRequestTest`의 테스트 본문을 `totalGames` 요청으로 교체한다. 기존 콘솔 인코딩 문제를 피하기 위해 문자열 값은 ASCII로 유지한다.

```java
@Test
void deserializesWithJsonCreatorConstructor() throws Exception {
    String json = """
            {
              "mode": "FIXED_SCHEDULE",
              "competitionName": "Fixed",
              "maleCount": 4,
              "femaleCount": 4,
              "courtCount": 2,
              "totalGames": 10,
              "seed": 136,
              "malePlayerNames": ["M1", "M2", "M3", "M4"],
              "femalePlayerNames": ["F1", "F2", "F3", "F4"]
            }
            """;

    CompetitionCreateRequest request = objectMapper.readValue(json, CompetitionCreateRequest.class);

    assertEquals("FIXED_SCHEDULE", request.getMode());
    assertEquals("Fixed", request.getCompetitionName());
    assertEquals(4, request.getMaleCount());
    assertEquals(4, request.getFemaleCount());
    assertEquals(2, request.getCourtCount());
    assertEquals(10, request.getTotalGames());
    assertEquals(136L, request.getSeed());
    assertEquals("M2", request.getMalePlayerNames().get(1));
    assertEquals("F2", request.getFemalePlayerNames().get(1));
}
```

- [ ] **Step 2: DTO 테스트를 실행해 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequestTest"
```

Expected: 아직 `CompetitionCreateRequest#getTotalGames()`가 없으므로 FAIL.

- [ ] **Step 3: 요청 DTO에서 `hours`를 `totalGames`로 교체**

`CompetitionCreateRequest`를 다음 형태로 수정한다.

```java
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
            @JsonProperty("femalePlayerNames") List<String> femalePlayerNames
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
    }
}
```

- [ ] **Step 4: DTO 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequestTest"
```

Expected: PASS.

- [ ] **Step 5: 커밋**

```powershell
git add src/main/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequest.java src/test/java/com/tennisfolio/Tennisfolio/matching/dto/CompetitionCreateRequestTest.java
git commit -m "feat: accept total games in competition creation request"
```

---

### Task 2: `rounds` 계산 및 `totalGames` 검증

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java`

- [ ] **Step 1: 기존 command service 테스트가 `totalGames`를 사용하도록 수정**

`createCompetition_returnsCompetitionAdminToken`에서 기존에 `hours` 의미로 쓰던 생성자 인자를 `totalGames` 값으로 둔다. `CLUB_SESSION`에서는 값 `1`을 유지한다. 이 값은 검증과 생성 크기에 사용되지 않는다.

```java
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
```

- [ ] **Step 2: 실패하는 고정 스케줄 `rounds` 계산 테스트 추가**

`CompetitionCommandServiceTest`에 다음 테스트를 추가한다.

```java
@Test
void createCompetition_derivesRoundsFromTotalGamesForFixedSchedule() {
    CompetitionCreateRequest request = new CompetitionCreateRequest(
            "FIXED_SCHEDULE",
            "Fixed",
            6,
            6,
            3,
            10,
            136L,
            null,
            null
    );
    Competition competition = new Competition("Fixed", 6, 6, 3, 4, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
    ScheduleResult scheduleResult = new ScheduleResult();
    Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

    when(competitionService.createCompetition(request, 4, 136L)).thenReturn(competition);
    when(scheduler.generateSchedule(6, 6, 3, 10, 136L)).thenReturn(scheduleResult);
    when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
    when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

    service.createCompetition(request);

    verify(competitionService).createCompetition(request, 4, 136L);
    verify(scheduler).generateSchedule(6, 6, 3, 10, 136L);
}
```

- [ ] **Step 3: 실패하는 검증 테스트 추가**

static import를 추가한다.

```java
import static org.junit.jupiter.api.Assertions.assertThrows;
```

다음 테스트를 추가한다.

```java
@Test
void createCompetition_rejectsNonPositiveTotalGamesForFixedSchedule() {
    CompetitionCreateRequest request = new CompetitionCreateRequest(
            "FIXED_SCHEDULE",
            "Fixed",
            4,
            4,
            2,
            0,
            136L,
            null,
            null
    );

    assertThrows(IllegalArgumentException.class, () -> service.createCompetition(request));
}

@Test
void createCompetition_rejectsTotalGamesGreaterThanCourtCountTimesTwenty() {
    CompetitionCreateRequest request = new CompetitionCreateRequest(
            "FIXED_SCHEDULE",
            "Fixed",
            4,
            4,
            2,
            41,
            136L,
            null,
            null
    );

    assertThrows(IllegalArgumentException.class, () -> service.createCompetition(request));
}
```

- [ ] **Step 4: command service 테스트를 실행해 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest"
```

Expected: `CompetitionCommandService`가 아직 `getHours()`를 사용하고 scheduler도 네 번째 정수를 `rounds`로 처리하므로 FAIL.

- [ ] **Step 5: `rounds` 계산과 검증 구현**

`CompetitionCommandService`에서 상수를 교체한다.

```java
private static final int MAX_PLAYER_COUNT = 40;
private static final int MAX_COURT_COUNT = 10;
private static final int MAX_ROUNDS = 20;
```

`createCompetition`의 `rounds` 계산과 scheduler 호출을 교체한다.

```java
int rounds = mode == Competition.CompetitionMode.CLUB_SESSION
        ? 1
        : calculateRounds(request.getTotalGames(), request.getCourtCount());
long seed = request.getSeed() != null
        ? request.getSeed()
        : ThreadLocalRandom.current().nextLong(1, 10000);

Competition competition = competitionService.createCompetition(request, rounds, seed);

ScheduleResult result = scheduler.generateSchedule(
        request.getMaleCount(),
        request.getFemaleCount(),
        request.getCourtCount(),
        mode == Competition.CompetitionMode.CLUB_SESSION ? rounds : request.getTotalGames(),
        seed
);
```

고정 스케줄 검증을 수정한다.

```java
if (mode == Competition.CompetitionMode.FIXED_SCHEDULE) {
    if (request.getTotalGames() <= 0) {
        throw new IllegalArgumentException("totalGames must be greater than 0");
    }
    if (request.getTotalGames() > request.getCourtCount() * MAX_ROUNDS) {
        throw new IllegalArgumentException("totalGames must be courtCount * " + MAX_ROUNDS + " or less");
    }
}
```

helper를 추가한다.

```java
private int calculateRounds(int totalGames, int courtCount) {
    return (int) Math.ceil((double) totalGames / courtCount);
}
```

- [ ] **Step 6: command service 테스트 실행**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandServiceTest"
```

Expected: Task 3에서 scheduler 의미까지 수정한 뒤 PASS. Task 3 전에 이 태스크만 실행하면 Mockito 테스트는 통과할 수 있지만, 다른 오래된 scheduler 호출 때문에 전체 컴파일은 실패할 수 있다.

- [ ] **Step 7: 커밋**

```powershell
git add src/main/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandService.java src/test/java/com/tennisfolio/Tennisfolio/matching/service/CompetitionCommandServiceTest.java
git commit -m "feat: derive fixed schedule rounds from total games"
```

---

### Task 3: 마지막 부분 고정 스케줄 라운드 생성

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/TennisMatchScheduler.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java`
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`

- [ ] **Step 1: 실패하는 마지막 부분 라운드 테스트 추가**

`TennisMatchSchedulerTest`에 다음 테스트를 추가한다.

```java
@Test
void generateSchedule_allowsPartialFinalRoundWhenTotalGamesDoesNotFillAllCourts() {
    TennisMatchScheduler scheduler = createScheduler();

    ScheduleResult result = scheduler.generateSchedule(6, 6, 3, 10, 136);

    assertEquals(10, result.matches.size());
    assertEquals(4, result.matches.stream().mapToInt(match -> match.round).max().orElseThrow());

    List<GameMatch> finalRoundMatches = result.matches.stream()
            .filter(match -> match.round == 4)
            .toList();

    assertEquals(1, finalRoundMatches.size());
    assertEquals(1, finalRoundMatches.get(0).court);

    for (int round = 1; round <= 4; round++) {
        int currentRound = round;
        Set<String> playersInRound = new HashSet<>();
        result.matches.stream()
                .filter(match -> match.round == currentRound)
                .forEach(match -> allPlayers(match).forEach(player ->
                        assertTrue(playersInRound.add(player.id), "Player appears more than once in round " + currentRound)
                ));
    }
}
```

- [ ] **Step 2: 기존 고정 스케줄 테스트가 총 게임 수를 전달하도록 수정**

기존 `generateSchedule(..., rounds, seed)` 호출에서 네 번째 인자를 총 게임 수로 바꾼다.

```java
ScheduleResult result = scheduler.generateSchedule(male, female, court, court * rounds, 136);
```

literal case는 다음처럼 수정한다.

```java
scheduler.generateSchedule(7, 9, 3, 12, 136);
scheduler.generateSchedule(1, 4, 1, 4, 136);
scheduler.generateSchedule(male, female, court, court * 4, 136);
scheduler.generateSchedule(8, 8, 3, 15, 136);
```

- [ ] **Step 3: scheduler 테스트를 실행해 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest"
```

Expected: `FixedScheduleGenerator`가 아직 네 번째 인자를 `rounds`로 해석해서 `court * totalGames`개 match를 만들기 때문에 FAIL.

- [ ] **Step 4: scheduler 메서드 파라미터 의미와 위임 수정**

`TennisMatchScheduler`에서 public 메서드 이름은 유지하되, 파라미터명을 바꿔 의미를 명확히 한다.

```java
public ScheduleResult generateSchedule(int male, int female, int court, int totalGames, long seed) {
    return fixedScheduleGenerator.generateSchedule(male, female, court, totalGames, seed);
}
```

- [ ] **Step 5: fixed generator가 총 게임 수를 사용하도록 수정**

`FixedScheduleGenerator`의 public 메서드를 수정한다.

```java
public ScheduleResult generateSchedule(int male, int female, int court, int totalGames, long seed) {
    int rounds = calculateRounds(totalGames, court);
    boolean allowRandom = shouldAllowRandomType(male, female, court, totalGames);

    try {
        return generateSchedule(male, female, court, totalGames, rounds, seed, allowRandom);
    } catch (NoSuchElementException e) {
        if (allowRandom || !canScheduleRandomType(male, female)) {
            throw e;
        }

        return generateSchedule(male, female, court, totalGames, rounds, seed, true);
    }
}
```

private 생성 메서드 시그니처와 총량 계산을 수정한다.

```java
private ScheduleResult generateSchedule(
        int male,
        int female,
        int court,
        int totalGames,
        int rounds,
        long seed,
        boolean allowRandom
) {
    this.random = new Random(seed);
    List<GamePlayer> players = createPlayers(male, female);

    int totalSlots = totalGames * 4;
    int maxGames = (int) Math.ceil((double) totalSlots / players.size());
```

중첩된 round/court loop를 수정한다.

```java
int remainingGames = totalGames;
for (int r = 1; r <= rounds; r++) {
    Set<GamePlayer> used = new HashSet<>();
    Set<GamePlayer> playedThisRound = new HashSet<>();
    Set<MatchType> roundTypes = new HashSet<>();
    int gamesInRound = Math.min(court, remainingGames);

    for (int c = 1; c <= gamesInRound; c++) {
        List<GamePlayer> availablePlayers = players.stream()
                .filter(p -> !used.contains(p))
                .toList();
```

생성된 match를 추가한 직후 남은 게임 수를 감소시킨다.

```java
result.matches.add(new GameMatch(r, c, best.candidate.type, best.candidate.teamA, best.candidate.teamB));
remainingGames--;
```

helper를 추가한다.

```java
private int calculateRounds(int totalGames, int court) {
    return (int) Math.ceil((double) totalGames / court);
}
```

- [ ] **Step 6: 배치 가능성 helper가 총 게임 수를 받는지 확인**

`FixedScheduleGenerator`에서 `shouldAllowRandomType`, `canFillGenderSlotsWithoutRandom`, `canFillMaleSlotsWithNormalTypes`의 파라미터명은 원하면 `totalMatches`로 유지해도 된다. 단, 호출부에서는 반드시 `totalGames`를 전달한다.

다음 줄들이 실제 총량을 사용하는지 확인한다.

```java
boolean allowRandom = shouldAllowRandomType(male, female, court, totalGames);
int totalSlots = totalMatches * 4;
```

- [ ] **Step 7: scheduler 테스트 실행**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest"
```

Expected: PASS.

- [ ] **Step 8: 커밋**

```powershell
git add src/main/java/com/tennisfolio/Tennisfolio/matching/service/TennisMatchScheduler.java src/main/java/com/tennisfolio/Tennisfolio/matching/service/fixed/FixedScheduleGenerator.java src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java
git commit -m "feat: generate fixed schedules by total games"
```

---

### Task 4: 컴파일 및 matching 집중 테스트

**Files:**
- Verify only.

- [ ] **Step 1: main Java 소스 컴파일**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: SUCCESS.

- [ ] **Step 2: test 소스 컴파일**

Run:

```powershell
.\gradlew.bat compileTestJava
```

Expected: SUCCESS. 남아 있는 `getHours()` 또는 오래된 생성자/테스트 호출을 잡는다.

- [ ] **Step 3: matching 테스트 실행**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.matching.*"
```

Expected: SUCCESS.

- [ ] **Step 4: matching 생성 경로의 오래된 `hours` 사용 검색**

Run:

```powershell
rg -n "getHours|\"hours\"|MAX_HOURS|ROUNDS_PER_HOUR" src/main/java/com/tennisfolio/Tennisfolio/matching src/test/java/com/tennisfolio/Tennisfolio/matching
```

Expected: competition 생성과 관련된 결과가 없어야 한다.

- [ ] **Step 5: 검증 중 발견된 정리 변경이 있으면 커밋**

Step 4에서 오래된 참조를 발견해 제거했다면 정리 변경을 커밋한다.

```powershell
git add src/main/java/com/tennisfolio/Tennisfolio/matching src/test/java/com/tennisfolio/Tennisfolio/matching
git commit -m "test: update matching tests for total games"
```

수정 사항이 없으면 이 커밋은 건너뛴다.

---

## 자체 검토

- 설계 반영: API 계약, 파생 `rounds`, 마지막 부분 라운드 생성, 실제 총 게임 수 기준 공정성 계산, 검증, 테스트를 모두 포함했다.
- placeholder 검사: 비어 있는 구현 단계나 임시 표시 문구가 없다.
- 타입 일관성: `CompetitionCreateRequest#getTotalGames()`는 Task 1에서 도입하고 이후 태스크에서 일관되게 사용한다. `TennisMatchScheduler.generateSchedule`은 메서드 이름과 인자 개수는 유지하지만 네 번째 인자의 의미를 `totalGames`로 바꾼다.
