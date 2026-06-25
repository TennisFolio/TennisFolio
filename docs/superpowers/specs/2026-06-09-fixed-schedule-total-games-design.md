# 고정 스케줄 총 게임 수 입력 설계

## 배경

현재 `FIXED_SCHEDULE` competition 생성은 `hours`를 입력받고, `hours * 2`로 `rounds`를 계산한다. 실제 생성되는 게임 수는 `courtCount * rounds`가 된다.

생성 API는 이제 시간 대신 모든 코트를 합산한 총 게임 수를 입력받아야 한다. 예를 들어 `courtCount = 3`, `totalGames = 10`이면 생성 스케줄은 총 10게임이어야 한다. 3게임짜리 라운드 3개와 1게임짜리 부분 라운드 1개가 생성된다.

## 결정 사항

외부 `FIXED_SCHEDULE` 생성 입력값으로 `totalGames`를 사용하고, 내부에 저장되는 값으로는 기존 `rounds`를 유지한다.

`rounds`는 생성된 게임을 담기 위해 필요한 스케줄 라운드 수라는 기존 도메인 의미를 유지한다. 계산식은 다음과 같다.

```text
rounds = ceil(totalGames / courtCount)
```

이번 변경에서는 새로운 `totalGames` 컬럼을 추가하지 않는다. 정확한 게임 수는 저장된 `Game` row 수로 표현하고, `Competition.rounds`는 스케줄을 그룹화하고 표시하는 데 계속 사용한다.

## API 계약

`CompetitionCreateRequest`는 `hours` 대신 `totalGames`를 받는다.

`FIXED_SCHEDULE`의 경우:

- `totalGames`는 모든 코트를 합산한 총 생성 게임 수를 의미한다.
- `totalGames`는 0보다 커야 한다.
- `totalGames`는 `courtCount * 20` 이하여야 한다.
- `rounds`는 `totalGames`와 `courtCount`로부터 계산한다.

`CLUB_SESSION`의 경우:

- 기존의 1라운드 생성 동작을 유지한다.
- `totalGames`는 검증이나 초기 스케줄 크기 결정에 사용하지 않는다.

## 생성 흐름

`CompetitionCommandService.createCompetition`은 mode를 판별하고, 요청을 검증하고, 파생값인 `rounds`를 계산한 뒤 `Competition`을 저장한다. 이후 요청받은 `totalGames`를 사용해 스케줄 생성을 호출한다.

`TennisMatchScheduler.generateSchedule`과 `FixedScheduleGenerator.generateSchedule`은 실제 고정 스케줄 게임 수를 입력받는다. 고정 스케줄 생성기는 라운드 번호를 계속 사용하되, 각 라운드에서는 남은 게임 수만큼만 생성한다.

```text
gamesInRound = min(courtCount, remainingGames)
```

`courtCount = 3`, `totalGames = 10`인 경우:

```text
round 1: court 1, court 2, court 3
round 2: court 1, court 2, court 3
round 3: court 1, court 2, court 3
round 4: court 1
```

따라서 마지막 라운드는 부분 라운드가 될 수 있다. 같은 라운드 안에서 한 플레이어가 중복 출전할 수 없다는 기존 규칙은 유지한다.

## 공정성 및 제약 조건

생성된 총 게임 수에 의존하는 계산은 `courtCount * rounds`가 아니라 `totalGames`를 사용해야 한다.

대상 계산은 다음을 포함한다.

- 총 매치 수
- 총 플레이어 슬롯 수
- 플레이어별 최대 게임 수
- 성별 슬롯 배치 가능성
- 매치 타입 배치 가능성

이렇게 해야 마지막 부분 라운드에 실제로 존재하지 않는 게임이 있는 것처럼 계산되는 문제를 막을 수 있다.

## 오류 처리

기존 요청 검증은 유지한다.

- `maleCount`와 `femaleCount`는 음수일 수 없다.
- `courtCount`는 0보다 커야 한다.
- `courtCount`는 기존 최대값 이하여야 한다.
- 전체 플레이어 수는 최소 4명이어야 한다.
- 전체 플레이어 수는 기존 최대값 이하여야 한다.
- 전체 플레이어 수는 최소 `courtCount * 4`명이어야 한다.

새로운 `FIXED_SCHEDULE` 검증은 다음과 같다.

- `totalGames <= 0`이면 `IllegalArgumentException`을 던진다.
- `totalGames > courtCount * 20`이면 `IllegalArgumentException`을 던진다.

기존 `hours` 검증은 고정 스케줄 생성 경로에서 제거한다.

## 테스트

다음 테스트를 수정하거나 추가한다.

- `CompetitionCreateRequest`가 `totalGames`를 역직렬화하는지 확인한다.
- `CompetitionCommandService`가 `courtCount = 3`, `totalGames = 10`에서 `rounds = 4`를 계산하는지 확인한다.
- `CompetitionCommandService`가 scheduler에 `totalGames = 10`을 전달하는지 확인한다.
- `FixedScheduleGenerator`가 `courtCount = 3`, `totalGames = 10`에서 정확히 10게임을 생성하는지 확인한다.
- 생성된 10게임 스케줄에서 4라운드에는 1게임만 있는지 확인한다.
- `FIXED_SCHEDULE`가 `totalGames <= 0`을 거절하는지 확인한다.
- `FIXED_SCHEDULE`가 `totalGames > courtCount * 20`을 거절하는지 확인한다.
- 기존 고정 스케줄 테스트는 `hours` 대신 `totalGames`를 사용하도록 수정한다.

## 범위 제외

이 설계는 `Competition`에 `totalGames` 컬럼을 추가하지 않는다.

이 설계는 `CLUB_SESSION` 다음 게임 생성 로직을 변경하지 않는다.

이 설계는 고정 스케줄에서 생성되는 게임 수 변경 외에 기존 game, entry, statistic 저장 모델을 변경하지 않는다.
