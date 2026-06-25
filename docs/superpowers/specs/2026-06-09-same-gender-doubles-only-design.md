# 남복/여복 전용 대진 생성 설계

## 배경

현재 `FIXED_SCHEDULE` competition 생성은 남녀 참가자가 모두 있을 때 `MIXED`, `MALE`, `FEMALE` 후보를 기본으로 만들고, 필요하면 `RANDOM_M3F1`, `RANDOM_M1F3` 후보도 사용한다.

프론트에서는 토글을 통해 전체 대진 생성 시 혼복 없이 남복/여복만 돌리는 옵션을 제공하려고 한다. 이 옵션이 켜지면 남녀가 모두 포함된 competition이라도 생성된 게임은 `MALE`, `FEMALE` 타입만 가져야 한다.

이번 범위는 `FIXED_SCHEDULE` 최초 생성에 한정한다. 현재 `FIXED_SCHEDULE`에서는 생성 후 게임을 추가하지 않으므로, 이 옵션을 `Competition` 엔티티나 DB에 저장하지 않는다. 생성된 `Game.matchType` 목록이 실제 결과를 설명한다.

## 목표

- `POST /api/competitions`에서 남복/여복 전용 생성 옵션을 받을 수 있게 한다.
- 옵션이 켜진 `FIXED_SCHEDULE` 생성은 `MALE`, `FEMALE` 후보만 사용한다.
- 남복/여복 전용으로 산술적으로 가능한 `FIXED_SCHEDULE` 요청은 greedy 선택 실패 때문에 생성 실패하지 않게 한다.
- 옵션이 꺼졌거나 누락된 요청은 기존 스케줄 생성 동작을 유지한다.
- `CLUB_SESSION` 다음 게임 생성 로직은 변경하지 않는다.
- 기존 공정성 점수는 유지한다. 즉 가능한 범위에서 같은 파트너, 같은 상대, 같은 4명 조합, 경기 수 불균형을 계속 피한다.

## API 계약

`CompetitionCreateRequest`에 boolean 필드를 추가한다.

```json
{
  "mode": "FIXED_SCHEDULE",
  "competitionName": "Weekend Doubles",
  "maleCount": 8,
  "femaleCount": 8,
  "courtCount": 2,
  "totalGames": 12,
  "sameGenderDoublesOnly": true
}
```

필드 이름은 `sameGenderDoublesOnly`를 사용한다.

- `true`: `FIXED_SCHEDULE` 생성 시 `MALE`, `FEMALE`만 허용한다.
- `false`: 기존 기본 정책을 사용한다.
- 누락: `false`와 동일하게 처리한다.
- `CLUB_SESSION`: 이번 범위에서는 값을 무시하고 기존 동작을 유지한다.

응답 DTO는 변경하지 않는다. competition 생성 응답은 기존처럼 `publicId`, `editToken`만 반환한다.

## 생성 정책

내부 스케줄러에는 boolean 조합을 직접 퍼뜨리지 않고 허용 경기 타입 집합을 전달한다.

기본 정책:

```text
MIXED, MALE, FEMALE
필요 시 RANDOM_M3F1, RANDOM_M1F3 자동 허용 또는 fallback
```

남복/여복 전용 정책:

```text
MALE, FEMALE
```

남복/여복 전용 정책에서는 `MIXED`, `RANDOM_M3F1`, `RANDOM_M1F3`를 생성하지 않는다. 후보가 부족해도 혼복이나 랜덤 타입으로 자동 우회하지 않는다.

예를 들어 남자 8명, 여자 0명인 경우에는 일반 정책과 남복/여복 전용 정책 모두 `MALE`만 생성될 수 있다. 여자 참가자가 없으므로 여복은 만들지 않지만, 1명 이상 3명 이하의 애매한 여자 그룹이 있는 것은 아니기 때문에 토글 사용을 막지 않는다.

남자 8명, 여자 8명인 경우에는 차이가 생긴다.

- 일반 정책: `MIXED`, `MALE`, `FEMALE`을 점수에 따라 섞어서 생성한다.
- 남복/여복 전용 정책: `MALE`, `FEMALE`만 생성한다.

### 남복/여복 전용 목표 계산

남복/여복 전용 정책은 경기 타입 수를 먼저 계산한 뒤 생성한다.

총 출전 슬롯은 `totalGames * 4`이고, 선수별 출전 목표는 다음 범위로 계산한다.

```text
minGames = floor(totalSlots / playerCount)
maxGames = ceil(totalSlots / playerCount)
extraSlots = totalSlots - playerCount * minGames
```

`minGames == maxGames`인 경우 모든 선수의 목표 출전 수는 동일하다. `minGames != maxGames`인 경우 각 선수는 가능한 한 `minGames` 또는 `maxGames` 안에서 배정한다.

남복/여복 전용에서는 성별별 총 슬롯이 4의 배수여야 한다.

```text
maleSlots = maleCount * minGames + maleExtraSlots
femaleSlots = femaleCount * minGames + femaleExtraSlots
maleExtraSlots + femaleExtraSlots = extraSlots
maleSlots % 4 == 0
femaleSlots % 4 == 0
maleGames = maleSlots / 4
femaleGames = femaleSlots / 4
maleGames + femaleGames = totalGames
```

`maleExtraSlots`는 `0..maleCount`, `femaleExtraSlots`는 `0..femaleCount` 범위에서 선택한다. 후보가 여러 개이면 선수별 출전 수 편차와 성별별 경기 수 균형을 우선해 선택한다. 생성 중 막히면 다음 후보 목표를 시도한다.

예를 들어 남자 6명, 여자 5명, 총 11경기이면 `totalSlots=44`, `playerCount=11`, `minGames=maxGames=4`이다. 따라서 `maleSlots=24`, `femaleSlots=20`이고 남복 6경기, 여복 5경기가 목표가 된다.

남자 6명, 여자 5명, 총 10경기이면 `totalSlots=40`, `minGames=3`, `extraSlots=7`이다. 가능한 배분 중 하나는 `maleExtraSlots=2`, `femaleExtraSlots=5`이고, 이때 `maleSlots=20`, `femaleSlots=20`이므로 남복 5경기, 여복 5경기가 목표가 된다. 모든 선수를 4경기로 강제하지 않는다.

성별별 목표 계산이 불가능하면 남복/여복 전용으로는 해당 요청을 만들 수 없으므로 생성 전에 `IllegalArgumentException`을 던진다. 다만 생성 알고리즘의 greedy 선택 실패는 불가능 조건으로 보지 않는다.

## 컴포넌트 변경

### CompetitionCreateRequest

`sameGenderDoublesOnly` 필드를 추가한다. JSON 역직렬화에서 누락된 값은 `false`로 취급한다.

### CompetitionCommandService

`createCompetition`에서 mode를 확인한 뒤 스케줄 생성 정책을 결정한다.

- `mode == FIXED_SCHEDULE && request.isSameGenderDoublesOnly()`이면 `MALE`, `FEMALE` 정책을 전달한다.
- 그 외에는 기존 기본 스케줄 생성 메서드를 사용하거나 기본 정책을 전달한다.

`Competition` 저장에는 이 값을 사용하지 않는다.

### TennisMatchScheduler

기존 메서드는 호환성을 위해 유지한다.

```java
generateSchedule(int male, int female, int court, int totalGames, long seed)
```

허용 타입을 받는 오버로드를 추가한다.

```java
generateSchedule(int male, int female, int court, int totalGames, long seed, Set<MatchType> allowedMatchTypes)
```

기존 메서드는 기본 정책으로 위임한다.

### FixedScheduleGenerator

허용 타입 집합을 받아 후보 생성에 전달한다.

기본 정책에서는 현재의 `allowRandom` 자동 판단과 fallback을 유지한다. 남복/여복 전용 정책에서는 `allowedMatchTypes`가 `MALE`, `FEMALE`로 고정되므로 random fallback을 실행하지 않는다.

남복/여복 전용 정책에서는 다음 전용 흐름을 사용한다.

1. 성별별 경기 수 목표 후보를 계산한다.
2. 목표 후보 순서대로 스케줄 생성을 시도한다.
3. 각 시도는 현재 greedy 선택으로 막히면 이전 선택으로 돌아가는 backtracking을 사용한다.
4. backtracking은 `MALE`, `FEMALE` 후보만 사용한다.
5. 모든 목표 후보가 실패하면 그때만 후보 없음 예외를 던진다.

이 흐름은 기존 기본 정책에는 적용하지 않는다.

### CandidateGenerator

허용 타입 집합에 포함된 타입만 후보를 만든다.

기존 `allowRandom` 기반 메서드는 유지하되, 내부에서 기본 허용 타입 집합으로 변환할 수 있다. 이렇게 하면 기존 테스트와 호출부를 크게 흔들지 않으면서 새 정책을 추가할 수 있다.

## 검증과 실패 처리

남복/여복 전용 옵션이 켜진 `FIXED_SCHEDULE` 요청은 생성 전에 성별별 최소 조건을 확인한다.

- 남자가 0명이면 남복을 생성하지 않는 조건으로 허용한다.
- 여자가 0명이면 여복을 생성하지 않는 조건으로 허용한다.
- 남자가 1명 이상 3명 이하이면 `IllegalArgumentException`을 던진다.
- 여자가 1명 이상 3명 이하이면 `IllegalArgumentException`을 던진다.

권장 메시지:

```text
sameGenderDoublesOnly requires each included gender to have at least 4 players
```

남복/여복 전용 정책은 성별별 목표 계산이 불가능한 요청을 생성 전에 거부한다.

권장 메시지:

```text
sameGenderDoublesOnly cannot allocate same-gender game counts for the requested player distribution
```

그 외 실제 조합 배치 문제는 backtracking으로 해결한다. 남복/여복 전용으로 산술적으로 가능한 요청은 단순 greedy 선택 실패 때문에 `No valid match candidate`로 끝나지 않아야 한다.

남복/여복 전용 정책에서는 실패 시 혼복이나 `RANDOM_*`으로 우회하지 않는다.

## 공정성

남복/여복 전용 옵션은 후보 타입만 제한한다. 기존 점수 계산은 유지한다.

따라서 가능한 후보가 충분할 때는 계속 다음을 피하려고 한다.

- 같은 파트너 반복
- 같은 상대 반복
- 같은 4명 조합 반복
- 특정 선수의 경기 수 과다
- 같은 라운드 내 중복 출전
- 연속 라운드 출전

다만 이것은 절대 금지가 아니라 점수 기반 선호다. 예를 들어 남자 4명만으로 여러 남복 게임을 생성하면 같은 4명 조합이 반복될 수밖에 없다. 남자 8명 이상처럼 후보가 많을수록 반복 회피가 잘 작동한다.

## 테스트 계획

### DTO

- `CompetitionCreateRequest`가 `sameGenderDoublesOnly`를 역직렬화하는지 확인한다.
- 필드가 누락되면 `false`로 취급되는지 확인한다.

### Command Service

- `FIXED_SCHEDULE + sameGenderDoublesOnly=true` 요청이 `MALE`, `FEMALE` 허용 정책으로 스케줄러를 호출하는지 확인한다.
- `sameGenderDoublesOnly=false` 또는 누락 시 기존 기본 스케줄 생성 호출이 유지되는지 확인한다.
- `CLUB_SESSION`에서는 `sameGenderDoublesOnly=true`여도 다음 게임 생성 정책에 영향을 주지 않는지 확인한다.
- 남자 또는 여자가 1명 이상 3명 이하인 남복/여복 전용 `FIXED_SCHEDULE` 요청이 실패하는지 확인한다.
- 남자 8명, 여자 0명처럼 한 성별만 있는 남복/여복 전용 `FIXED_SCHEDULE` 요청은 허용되는지 확인한다.

### Scheduler / Generator

- 남자와 여자가 모두 있는 입력에서 남복/여복 전용 정책은 `MALE`, `FEMALE` 타입만 생성하는지 확인한다.
- 남자 8명, 여자 0명 입력에서 기본 정책과 남복/여복 전용 정책 모두 `MALE`만 생성 가능한지 확인한다.
- 남복/여복 전용 정책에서 후보가 부족할 때 `MIXED` 또는 `RANDOM_*`으로 fallback하지 않는지 확인한다.
- 남자 6명, 여자 5명, 코트 2개, 총 11경기, 남복/여복 전용 요청이 생성되는지 확인한다.
- 남자 6명, 여자 5명, 코트 2개, 총 10경기, 남복/여복 전용 요청이 모든 선수를 4경기로 강제하지 않고 생성되는지 확인한다.
- 남복/여복 전용 생성 결과가 계산된 `MALE`, `FEMALE` 경기 수 목표를 만족하는지 확인한다.
- 특정 seed에서 greedy 선택이 막혀도 backtracking 또는 다음 목표 후보 시도로 생성되는지 확인한다.
- 기존 기본 정책 테스트가 계속 통과하는지 확인한다.

## 범위 제외

- `Competition` 엔티티나 `tb_competition`에 남복/여복 전용 여부를 저장하지 않는다.
- `CompetitionDetailResponse`에 이 옵션을 노출하지 않는다.
- `CLUB_SESSION` 다음 게임 생성 정책은 변경하지 않는다.
- 생성된 스케줄을 나중에 같은 옵션으로 재생성하는 기능은 포함하지 않는다.
- 프론트 토글 UI 구현은 이번 백엔드 설계 범위에 포함하지 않는다.
