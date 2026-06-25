# Commit Guide

## Commit Principle

커밋은 "AI가 만든 코드"를 저장하는 순간이 아니라, 내가 이해하고 검증해서 책임질 수 있는 순간에 한다.

## Commit Criteria

커밋은 다음 조건을 모두 만족할 때만 한다.

- 사용자 기능 흐름 기준으로 의미가 있다.
- 실행 가능한 상태다.
- 테스트 또는 수동 검증이 끝났다.
- spec, plan, code가 서로 일치한다.
- 커밋 메시지만 보고 변경 의도를 이해할 수 있다.

## Commit Unit

좋은 커밋 단위:

- API 엔드포인트 추가
- 인증 로직 구현
- 보안 필터 연결
- UI 화면 추가
- API 연동
- 실패 흐름 보정
- 문서와 구현 동기화

피해야 할 커밋 단위:

- "백엔드 전체"
- "프론트 전체"
- "AI가 만든 것 저장"
- 테스트하지 않은 대량 변경
- 문서와 코드가 다른 상태의 임시 저장

## Message Format

기본 형식:

```text
<type>: <user-visible or behavior-focused summary>
```

자주 쓰는 type:

- `feat`: 새 기능 또는 사용자 행동 추가
- `fix`: 버그 수정 또는 실패 흐름 보정
- `test`: 테스트 추가 또는 수정
- `docs`: 문서만 변경
- `refactor`: 동작 변경 없는 구조 개선
- `chore`: 빌드, 설정, 정리 작업

## Examples

```text
feat: add login endpoint
feat: implement authentication logic
feat: configure security filter
feat: add JWT generation
feat: create login form
feat: integrate login API
fix: handle invalid login credentials
test: cover login success and failure
docs: add login feature spec
```

## Commit Body

다음 중 하나라도 해당하면 본문을 쓴다.

- 왜 이렇게 설계했는지 설명이 필요하다.
- spec 또는 plan도 함께 바뀌었다.
- 테스트하지 못한 항목이 있다.
- 후속 작업이 명확히 남아 있다.

예시:

```text
fix: update login flow and align spec

- move token delivery to response body
- update feature spec validation checklist
- keep existing refresh-token cookie behavior
```

## Pre-Commit Checklist

- [ ] 변경 내용을 직접 이해했다
- [ ] 관련 테스트를 실행했다
- [ ] 실패 흐름을 확인했다
- [ ] 기능 문서가 최신 상태다
- [ ] 커밋 메시지가 사용자 기능 흐름을 설명한다
