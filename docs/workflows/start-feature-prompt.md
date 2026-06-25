# Start Feature Prompt

새 컨텍스트에서 기능 개발을 시작할 때 아래 프롬프트를 사용한다.

```text
이 프로젝트의 기능 개발 워크플로우를 따라 `<기능명>` 개발을 시작해줘.

먼저 아래 문서를 읽고 규칙을 따라줘.

- AGENTS.md
- docs/workflows/development-workflow.md
- docs/workflows/commit-guide.md
- docs/workflows/qa-checklist.md
- docs/features/_template.md

진행 규칙:

- superpowers는 spec/plan 작성 절차로 사용한다.
- `superpowers:brainstorming`은 요구사항, 사용자 흐름, 설계 검토, 검증 기준을 정리할 때 사용한다.
- `superpowers:writing-plans`는 구현 순서, 파일 범위, 테스트 설명, 검증 명령, 커밋 단위를 정리할 때 사용한다.
- 별도 `docs/superpowers/specs` 또는 `docs/superpowers/plans` 파일은 만들지 않는다.
- 기능 문서는 `docs/features/<feature-name>.md` 하나로 통합한다.
- plan에는 구현 코드와 테스트 코드 전문을 넣지 않는다.
- plan에는 구현 위치, 책임, 테스트 설명, 검증 명령, 기대 결과, 커밋 단위만 적는다.
- 테스트 코드는 실제 테스트 파일에 작성한다.
- 실행한 검증 명령과 결과는 feature 문서의 `Verification Log`에 기록한다.
- 구현 전에 spec과 plan을 먼저 작성하고 내 확인을 받아라.
- 코드 변경은 내가 승인한 뒤 시작해라.
- Java/Gradle/npm 빌드나 테스트는 내가 명시적으로 요청하거나 승인한 경우에만 실행해라.
```

## Short Version

```text
docs/workflows/start-feature-prompt.md를 따라 `<기능명>` 개발을 시작해줘. 구현 전에 `docs/features/<feature-name>.md`에 spec과 plan을 먼저 작성하고 내 확인을 받아.
```

## Expected Output Before Implementation

새 기능을 시작한 컨텍스트는 코드 변경 전에 다음을 먼저 제시해야 한다.

- 읽은 문서 목록
- 생성 또는 수정할 feature 문서 경로
- spec 요약
- design review 쟁점
- plan / commit units 초안
- 테스트 설명과 검증 명령
- 사용자 승인 요청
