# Feature: 클럽 모임 실력 균형 대진

## 1. Goal

클럽 모임 관리자가 고정 대진표를 만들 때 실력 기반 대진을 선택하면, 팀별 실력 LEVEL 합의 차이가 작은 경기가 우선 생성된다. 혼복 제한과 함께 사용할 수 있으며, 일반 모임과 일반 Competition 생성 흐름은 바꾸지 않는다.

## 2. User Flow

```text
클럽 모임 관리자: 혼복 제한/실력 기반 대진 옵션 선택
-> 실력 기반 선택 시 참석자 LEVEL 유무 검증
-> 미설정자 존재: 생성 중단 및 이름 안내
-> 모두 설정됨: 기존 제약과 LEVEL 균형 점수를 함께 적용해 Competition/Schedule 생성
-> 경기표 표시
```

## 3. Spec

- 클럽 모임 대진 생성 요청에 `skillBalancedSchedule`(기본 `false`) 옵션을 추가한다.
- 옵션은 `sameGenderDoublesOnly`와 독립적이며 두 옵션을 함께 선택할 수 있다.
- `skillBalancedSchedule=true`이면 참석 처리된 모든 참가자의 `clubSkillTierId`를 조회하고, 해당 Club의 유효한 LEVEL을 사용한다.
- LEVEL이 없거나 해당 클럽의 등급이 아닌 참가자가 있으면 Competition/Schedule을 저장하지 않고 생성 요청을 실패시킨다. 사용자에게 미설정 참가자 이름을 안내한다.
- 모든 LEVEL이 있으면 `generateSchedule`의 실력 기반 전용 입력으로 참가자별 LEVEL을 전달한다. 기존 인원수 기반 시그니처는 하위 호환을 위해 유지한다.
- `SkillBalanceCalculator`가 후보 경기의 `abs(teamALevelSum - teamBLevelSum)`을 계산하고, `ScoreCalculator`가 그 값을 기존 후보 점수에서 감점한다. 차이 0이 가장 우선이며 차이가 커질수록 우선순위가 낮아진다.
- 이는 소프트 제약이다. 차이 1 이하 후보가 없어도 기존 성별, 경기 수, 라운드, 중복 최소화 제약을 만족하는 후보 중 가장 균형적인 경기를 선택한다.
- `sameGenderDoublesOnly=false`와 `skillBalancedSchedule=true`가 함께 선택되면 성별과 관계없이 모든 복식 조합을 후보로 생성한다. LEVEL이 남성 `3, 1`, 여성 `3, 1`이면 `남성(3+1) : 여성(3+1)`도 균형 후보가 된다.
- 성별 무관 후보는 실제 팀 구성을 기준으로 `MALE`, `FEMALE`, `MIXED`, `RANDOM_M3F1`, `RANDOM_M1F3`, `M2F2_SPLIT` 경기 유형을 결정한다. 실력 기반 옵션이 꺼진 기존 생성 규칙은 바꾸지 않는다.
- 일반 모임, 일반 `POST /api/competitions`, 다음 경기 생성(CLUB_SESSION)은 기존 동작을 유지한다.
- `CompetitionEntry`에는 `clubSkillTierId`나 LEVEL을 저장하지 않는다. 이번 기능은 생성 시점의 입력값만 사용한다.
- 다음 경기 생성 화면 버튼 제거는 이번 범위에서 제외한다.

## 4. Design Review

### Responsibility

- Controller / API: 기존 모임 대진 생성 API가 `skillBalancedSchedule`을 수신한다.
- Service / Application: `MeetingCompetitionCreateService`가 클럽 모임 여부와 참석자 LEVEL 완전성을 검증하고, 참가자별 LEVEL을 Competition 생성 입력으로 전달한다.
- Domain: `GamePlayer`/고정 Schedule 생성기는 선택적으로 LEVEL을 보유한다. `SkillBalanceCalculator`는 팀 합 차이를 계산하고 `ScoreCalculator`는 이를 후보 점수에 반영한다.
- Repository / Persistence: `ClubSkillTierRepository`로 필요한 등급을 일괄 조회한다. 새 스키마나 `CompetitionEntry` 컬럼은 추가하지 않는다.
- Frontend / UI: 클럽 모임 관리 화면의 기존 혼복 제한 옵션 옆에 실력 기반 대진 토글과 미설정 안내를 추가한다. 일반 모임에는 노출하지 않는다.

### Generation Architecture

```text
MeetingCompetitionCreateService
  -> ScheduleGenerationRequest(participants, courtCount, totalGames, seed, options)
  -> TennisMatchScheduler
  -> FixedScheduleGenerator
       -> generateSchedule 또는 generateSameGenderOnlySchedule
       -> 공통 후보 점수 계산
            -> ScoreCalculator
            -> SkillBalanceCalculator (skillBalancedSchedule=true일 때만)
```

- `ScheduleGenerationOptions`는 `sameGenderDoublesOnly`, `skillBalancedSchedule`을 보유한다. 혼복 제한은 항상 성별 분리 후보만 허용하는 하드 제약이다. 실력 기반 옵션은 후보의 점수 우선순위를 결정하며, 혼복 제한이 꺼진 경우 성별 무관 후보 생성을 활성화한다.
- `ScheduleParticipant`는 `id`, `gender`, `skillLevel`만 보유하는 생성 입력 모델이다. `MeetingAttendance`, `ClubSkillTier`, `CompetitionEntry` 같은 JPA 엔티티를 matching 모듈에 전달하지 않는다.
- `MeetingCompetitionCreateService`가 참석자와 등급 엔티티를 `ScheduleParticipant`로 변환한다. 실력 기반 옵션이 꺼져 있으면 LEVEL 없이 전달할 수 있고, 켜져 있으면 변환 전에 LEVEL 완전성을 검증한다.
- `TennisMatchScheduler`는 새 요청 모델을 받는 실력 기반용 `generateSchedule`을 제공한다. 기존 인원수 기반 메서드는 기본 옵션 요청으로 위임해 기존 호출자를 유지한다.
- `FixedScheduleGenerator`는 혼복 제한 여부에 따라 기존의 두 생성 경로를 선택한다. 두 경로는 동일한 후보 점수 계산을 호출하므로, 실력 점수 적용이 한 경로에서 누락되지 않는다.
- `SkillBalanceCalculator`는 `MatchCandidate`의 팀별 LEVEL 합과 절대 차이를 계산한다. `ScoreCalculator`가 해당 차이를 감점으로 합산하며, 실력 기반 옵션이 꺼져 있으면 호출하지 않는다.
- 따라서 사용자에게는 혼복 제한/실력 기반의 네 조합이 제공된다. 내부에는 동성 복식 경로, 기존 성별 규칙 경로, 성별 무관 실력 경로와 선택적 공통 점수 규칙만 유지된다.

### Review Questions

- 실력 점수는 후보 선택만 보정하므로 기존 대진 성립성과 경기 수 균형 제약을 훼손하지 않는다.
- LEVEL 검증은 모임-클럽 경계에서 수행해 일반 Competition 도메인에 클럽 의존성을 추가하지 않는다.
- 생성기는 실력 기반 전용 `generateSchedule` 입력으로 참가자별 숫자 LEVEL을 받아, 향후 다른 실력 정보원에도 재사용 가능하게 한다.
- 일반 모임과 기존 인원수 기반 호출은 기본 옵션 요청으로 변환되므로 호환성을 유지한다.
- LEVEL의 영구 스냅샷, 재생성, 다음 경기 생성 변경은 현재 요구에 포함하지 않는다.

## 5. Plan / Commit Units

### 추가 파일

- `src/main/java/com/tennisfolio/Tennisfolio/matching/domain/ScheduleGenerationRequest.java`: 참가자, 코트 수, 경기 수, seed, 옵션을 묶는 생성 입력.
- `src/main/java/com/tennisfolio/Tennisfolio/matching/domain/ScheduleGenerationOptions.java`: 혼복 제한과 실력 기반 대진 여부.
- `src/main/java/com/tennisfolio/Tennisfolio/matching/domain/ScheduleParticipant.java`: ID, 성별, LEVEL만 가진 생성용 참가자 값 객체.
- `src/main/java/com/tennisfolio/Tennisfolio/matching/engine/SkillBalanceCalculator.java`: 팀 LEVEL 합 및 차이 계산.
- `src/test/java/com/tennisfolio/Tennisfolio/matching/engine/SkillBalanceCalculatorTest.java`: LEVEL 합 차이 계산 단위 테스트.

### 수정 파일

- `matching/domain/GamePlayer.java`: 생성 중 사용할 선택적 LEVEL을 보유한다.
- `matching/domain/MatchType.java`: 자동 생성 가능한 `M2F2_SPLIT` 유형을 추가한다.
- `matching/engine/CandidateGenerator.java`: 혼복 제한이 꺼진 실력 기반 대진에서 성별 무관 4인 조합과 3가지 팀 분할을 모두 생성하고, 팀 구성에 맞는 경기 유형을 부여한다.
- `matching/engine/ScoreCalculator.java`: 실력 기반 옵션일 때 `SkillBalanceCalculator` 감점을 합산한다.
- `matching/service/fixed/FixedScheduleGenerator.java`: 새 생성 입력을 받고 일반/동성 복식 두 경로에서 공통 점수 계산을 사용한다.
- `matching/service/TennisMatchScheduler.java`: 새 `ScheduleGenerationRequest` 기반 `generateSchedule`을 노출하고 기존 메서드는 기본 요청으로 위임한다.
- `matching/service/GameService.java`: 생성된 `M2F2_SPLIT`을 엔티티 경기 유형으로 저장한다.
- `meeting/dto/MeetingCompetitionCreateRequest.java`: `skillBalancedSchedule` 요청 필드를 추가한다.
- `meeting/service/MeetingCompetitionCreateService.java`: 클럽 참석자 LEVEL을 검증하고 생성 입력으로 변환한다.
- `matching/service/CompetitionCommandService.java`: 모임에서 전달한 생성 입력을 실제 Schedule 생성에 사용한다.
- `src/tennisFolio/src/page/MeetingManage.jsx`: 클럽 모임의 실력 기반 옵션 상태와 생성 요청을 관리한다.
- `src/tennisFolio/src/components/meeting/manage/MeetingManageOperationsPanel.jsx`: 옵션 상태를 대진 섹션으로 전달한다.
- `src/tennisFolio/src/components/meeting/manage/MeetingCompetitionPanel.jsx`: 혼복 제한 옆에 실력 기반 대진 토글을 표시한다.
- `src/tennisFolio/src/utils/clubApi.js`, `src/tennisFolio/src/utils/meetingApi.js`: `skillBalancedSchedule` payload를 전송한다.
- `src/test/java/com/tennisfolio/Tennisfolio/matching/TennisMatchSchedulerTest.java`: 4개 옵션 조합, 성별 무관 후보, `M2F2_SPLIT`을 포함한 팀 구성별 유형 판정, 실력 균형 후보를 검증한다.
- `src/test/java/com/tennisfolio/Tennisfolio/meeting/service/MeetingCompetitionCreateServiceTest.java`: LEVEL 완전성 검증과 생성 입력 전달을 검증한다.

### 구현 순서

- [x] `feat: add schedule generation input and skill scoring` - 구현 완료, Java 테스트 실행 대기
  - 구현: 추가 파일의 입력/계산 모델과 수정 파일의 후보 생성·점수 계산을 반영한다. 기존 인원수 기반 `generateSchedule`은 기본 옵션 요청으로 위임한다.
  - 테스트/검증: 차이 0 우선, 불가 시 최소 차이, 혼복 제한과 동시 적용, 성별 무관 후보와 팀 구성별 유형(`M2F2_SPLIT` 포함), 옵션 미선택 하위 호환을 검증한다. `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.matching.TennisMatchSchedulerTest --tests com.tennisfolio.Tennisfolio.matching.engine.SkillBalanceCalculatorTest` 실행을 제안한다.

- [x] `feat: validate club attendee tiers and pass schedule input` - 구현 완료, Java 테스트 실행 대기
  - 구현: 클럽 요청에서만 LEVEL을 검증하고, 통과 시에만 내부 생성 입력을 Competition 생성 흐름에 전달한다.
  - 테스트/검증: 전체 설정 성공, 미설정자 실패, 타 클럽 등급 실패, 옵션 미선택 기존 생성, 일반 모임 비영향을 검증한다. `./gradlew.bat test --tests com.tennisfolio.Tennisfolio.meeting.service.MeetingCompetitionCreateServiceTest` 실행을 제안한다.

- [x] `feat: expose skill-balanced option in club meeting management` - 구현 완료, 수동 UI 확인 대기
  - 구현: 클럽 모임 UI와 API payload에 토글을 연결하고 기존 notice 흐름으로 서버 오류를 표시한다. 일반 모임 UI와 다음 경기 생성 버튼은 바꾸지 않는다.
  - 테스트/검증: 프론트엔드 테스트 코드는 작성하거나 실행하지 않는다. 클럽 모임에서 네 옵션 조합을 수동 확인하고, 일반 모임에서는 새 토글이 보이지 않는지 확인한다.

커밋은 사용자의 별도 요청이 있을 때만 위 의미 단위로 준비한다.

## 6. Development Validation

- [ ] 클럽 모임에서 옵션을 켜고 모든 참석자 LEVEL이 있으면 대진이 생성된다.
- [ ] 옵션을 켠 상태에서 LEVEL 미설정자가 있으면 생성되지 않고 대상자가 안내된다.
- [ ] 옵션을 끄면 LEVEL 유무와 관계없이 기존 대진이 생성된다.
- [ ] 혼복 제한과 실력 기반 대진을 함께 켜면 남복/여복 범위 안에서 균형 점수가 적용된다.
- [ ] 혼복 제한을 끄고 실력 기반 대진을 켜면 성별과 관계없는 복식 조합 중 LEVEL 합 차이가 작은 후보가 선택된다.
- [ ] 일반 모임과 일반 Competition 생성 흐름은 변경되지 않는다.
- [ ] 관련 Java 테스트를 실행한다. 프론트엔드 테스트/빌드는 사용자 요청이 없으므로 실행하지 않는다.

## 7. Final QA Checklist

- [ ] 클럽 모임 관리자가 옵션 상태와 실패 사유를 이해할 수 있다.
- [ ] 미설정 참가자 이름을 보고 등급을 보완한 뒤 대진 생성을 다시 시도할 수 있다.
- [ ] 기존 혼복 제한 대진 생성이 유지된다.
- [ ] 테스트 결과와 수동 검증 결과를 PR에 설명할 수 있다.

## 8. Change Log

| Date | Change | Reason |
|---|---|---|
| 2026-08-03 | 초기 작성 | 클럽 모임 실력 균형 대진 개발 시작 |
| 2026-08-03 | 구현 반영 | 클럽 전용 LEVEL 검증, 실력 균형 후보, 관리 화면 옵션 연결 |
| 2026-08-03 | 클럽원 등급 동기화 | 클럽원 추가·게스트 전환 시 회원 등급을 참석자에 복사 |
| 2026-08-03 | 모임장 등급 동기화 | 클럽 모임에서 모임장 첫 참석 처리 시 회원 등급을 참석자에 복사 |
| 2026-08-03 | 생성 키 정합성 수정 | 실력 기반 Schedule 참가자 ID를 CompetitionEntry 내부 키(`M1`/`F1`)와 일치 |
| 2026-08-03 | GameEntry ID 연결 | 생성 Schedule의 GamePlayer에 CompetitionEntry ID를 주입하고 ID로 저장 |
| 2026-08-03 | 참석자 등급 표시 | 클럽 모임의 모든 참석자 옆에 등급 또는 미정 상태 표시 |
| 2026-08-03 | 실력 균형 우선순위 | 실력 기반 후보는 LEVEL 합 차이를 먼저 최소화하고 동률에 기존 점수 적용 |
