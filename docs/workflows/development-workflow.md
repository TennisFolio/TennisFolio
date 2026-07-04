# AI 기반 개발 워크플로우

## Core Principles

- 커밋은 AI가 아니라 내가 책임지는 순간에 한다.
- 기준은 레이어가 아니라 사용자 기능 흐름이다.
- 완벽보다 안정성과 의미 있는 단위가 먼저다.
- spec, plan, code는 항상 같은 truth를 유지한다.
- QA는 마지막 점검이고, 개발 중 검증이 핵심이다.

## Standard Flow

```text
브랜치 생성
-> 기능 spec 작성
-> phase / commit 단위 plan 작성
-> 설계 검토
-> phase별 구현, 이해, 테스트, 커밋 반복
-> 전체 통합 테스트
-> 최종 QA
-> PR
```

## Feature Document Rule

기능 문서는 `docs/features/<feature-name>.md`에 둔다.

새 기능을 시작할 때는 `docs/features/_template.md`를 복사해서 사용한다. 하나의 기능 문서 안에 spec, flow, validation, plan, commit unit을 함께 관리해서 문서와 코드가 분리되지 않게 한다.

## Superpowers Usage Rule

superpowers는 spec과 plan을 작성하기 위한 사고 절차로 사용한다.

- `superpowers:brainstorming`: 요구사항, 사용자 흐름, 설계 검토, 검증 기준을 정리할 때 사용한다.
- `superpowers:writing-plans`: 구현 순서, 파일 범위, 테스트 설명, 검증 명령, 커밋 단위를 정리할 때 사용한다.

단, 산출물은 `docs/features/<feature-name>.md` 하나에 통합한다. 기본 저장 위치인 `docs/superpowers/specs`와 `docs/superpowers/plans`에는 별도 spec 또는 plan 파일을 만들지 않는다.

## Development Loop

각 phase는 다음 순서로 진행한다.

```text
AI 생성
-> 코드 이해
-> 테스트 또는 수동 검증
-> spec / plan 갱신 여부 확인
-> 커밋
```

커밋 전에 다음 질문에 모두 답할 수 있어야 한다.

- 이 변경은 사용자 기능 흐름에서 어떤 의미가 있는가?
- 지금 상태로 실행 가능한가?
- 어떤 테스트 또는 검증으로 확인했는가?
- 문서와 코드가 서로 다른 말을 하고 있지 않은가?

## Plan Writing Rule

plan은 실행 순서와 검증 기준을 명확히 하기 위한 문서다. 구현 코드나 테스트 코드를 길게 붙여 넣는 문서가 아니다.

plan에 포함할 것:

- 구현할 책임과 파일 범위
- 테스트해야 할 시나리오
- 실행할 테스트 명령
- 기대 결과
- 커밋 단위

plan에 포함하지 않을 것:

- 테스트 코드 전문
- 아직 검토하지 않은 AI 생성 코드
- 실제 구현과 쉽게 어긋날 수 있는 긴 코드 블록

테스트는 "어떤 테스트를 작성하고 무엇을 검증할지" 설명한다. 실제 테스트 코드는 구현 단계에서 작성하고, 커밋 전에 실행 결과로 검증한다.

## Token Budget Rule

feature 문서는 짧게 유지한다. 목표는 120줄 이하이며, 150줄을 넘으면 중복 설명을 줄인다. 220줄을 넘으면 별도 사용자 흐름으로 나눌 수 있는지 검토한다.

feature 문서에 매번 공통 체크리스트를 복사하지 않는다. 공통 기준은 workflow 문서를 링크로 참조한다.

```md
Definition of Done: docs/workflows/development-workflow.md#definition-of-done
QA 기준: docs/workflows/qa-checklist.md
Commit 기준: docs/workflows/commit-guide.md
```

plan은 commit unit당 3줄을 기본으로 한다.

```md
- [ ] `feat: add login endpoint`
  - 구현: `AuthController` POST `/api/auth/login`
  - 테스트: 성공 200, 실패 401
  - 검증: `.\gradlew.bat test --tests ...AuthControllerTest`
```

완료된 plan 항목은 상세 설명을 계속 늘리지 않는다. 필요한 검증 이력은 feature 문서의 `Verification Log`에 남긴다.

```md
- [x] `feat: add login endpoint` - done, verified
```

다른 컨텍스트에서 시작할 때는 긴 설명 대신 `docs/workflows/start-feature-prompt.md`의 짧은 프롬프트를 사용한다.

## Design Review

설계 검토의 목적은 "이 구조가 맞는가?"를 개발 전에 확인하는 것이다.

검토 기준:

- 책임 분리: Controller, Service, Domain, Persistence, Security, UI의 역할이 명확한가?
- 확장 가능성: 다음 요구사항이 와도 변경 범위가 제한되는가?
- 중복 여부: 같은 판단이나 변환이 여러 곳에 흩어지지 않았는가?
- 과설계 여부: 지금 필요하지 않은 추상화나 계층을 만들고 있지 않은가?
- 테스트 가능성: 핵심 로직을 독립적으로 검증할 수 있는가?

## Testing Strategy

테스트는 개발 중에 한다. 최종 QA는 테스트를 대체하지 않는다.

테스트는 사용자 기능 흐름에서 실제로 깨질 수 있는 계약을 검증한다.

- 핵심 비즈니스 규칙과 상태 변경은 service/domain 테스트로 검증한다.
- repository query와 entity mapping은 persistence 테스트로 검증한다.
- controller 테스트는 route, JSON binding, HTTP status, security/auth 계약을 Spring MVC 또는 통합 테스트로 검증할 때 작성한다.
- controller가 request를 service에 그대로 전달하는 얇은 Mockito 테스트는 기본으로 작성하지 않는다.

권장 순서:

```text
작은 구현
-> 관련 테스트 실행
-> 실패 흐름 확인
-> 문서 갱신
-> 커밋
```

테스트가 어려운 경우에는 수동 검증 결과를 기능 문서나 PR 설명에 남긴다.

## Final QA

최종 QA는 기능 완료 후, PR 직전에 사용자 흐름 기준으로 실행한다.

확인할 것:

- 사용자가 목표 행동을 완료할 수 있는가?
- 실패 흐름이 이해 가능한가?
- 기존 핵심 기능이 깨지지 않았는가?
- 테스트로 확인한 내용과 테스트하지 못한 내용이 분명한가?

gstack `/qa`는 최종 검증 도구로 사용한다. 개발 중 반복 사용은 피하고, 기능 완료 직후 또는 PR 직전에 사용한다.

## Change Handling

설계나 요구사항이 바뀌면 spec, plan, code를 함께 맞춘다.

예시 커밋:

```text
fix: update login flow and align spec
```

커밋 본문에는 바뀐 이유와 문서 갱신 여부를 적는다.

## Monorepo Rule

FE / BE 레이어가 아니라 사용자 기능 흐름 단위로 작업을 나눈다.

예시:

```text
feat: add login API
feat: create login UI
feat: integrate login API
```

레이어별로 나누더라도 각 커밋은 실행 가능하고 검증 가능해야 한다.
