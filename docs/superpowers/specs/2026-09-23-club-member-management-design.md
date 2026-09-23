# 클럽 회원 관리 설계

## 목표와 범위

클럽 ADMIN이 대시보드에서 회원을 조회·등록·수정하고, 활동·휴면·탈퇴·복구 상태를 관리한다. 회원 생성과 엑셀 붙여넣기 등록은 항상 활동 상태로 만든다.

회비·납부 이력·상태 변경 이력과 기존 연락처/일반 메모의 노출·수정은 이번 범위에서 제외한다. 기존 메모 데이터는 삭제하지 않는다.

## 사용자 흐름

```text
ADMIN -> 대시보드 > 회원 관리 -> 상태/이름으로 조회
      -> 회원 추가 | 엑셀 붙여넣기 등록 | 회원 상세 수정
      -> 활동/휴면/탈퇴 변경 또는 탈퇴 회원 복구

일반 회원 -> 클럽 상세 > 활동 회원의 읽기 전용 명단 조회
```

## 회원 데이터와 상태

- `memberStatus`: `ACTIVE`, `SUSPENDED`, `WITHDRAWN`
- `joinedAt`: 실제 가입일, 선택 `LocalDate`
- `suspendedAt`: 현재 휴면 시작일, 선택 `LocalDate`
- `leftAt`: 현재 탈퇴 시작일, 선택 `LocalDate`
- `birthYear`: 출생연도, 선택 `Integer`

날짜 API 형식은 `YYYY-MM-DD`이며 DTO와 엔티티는 `LocalDate`를 사용한다. 기존 데이터의 세 날짜와 출생연도는 `NULL`로 유지한다. 기존 활성 회원은 `ACTIVE`, 기존 비활성 회원은 `WITHDRAWN`으로 이관한다.

| 전환 | 날짜 처리 |
|---|---|
| `ACTIVE → SUSPENDED` | `suspendedAt`을 오늘로 설정하며 관리자가 과거 날짜로 바꿀 수 있음 |
| `SUSPENDED → ACTIVE` | `suspendedAt`을 비움 |
| `ACTIVE/SUSPENDED → WITHDRAWN` | `leftAt`을 오늘로 설정하며 관리자가 과거 날짜로 바꿀 수 있음; 휴면이면 `suspendedAt`을 비움 |
| `WITHDRAWN → ACTIVE` | `leftAt`을 비움 |

클럽 내 이름은 상태와 관계없이 유일하다. 탈퇴 회원과 같은 이름은 신규 생성할 수 없고 기존 회원을 복구해야 한다. 마지막 활동 관리자는 일반 회원 강등·휴면·탈퇴할 수 없다.

## 화면과 경로

- `/clubs/:clubPublicId/dashboard/members`: ADMIN 회원 관리 목록. 활동/휴면/탈퇴/전체 필터, 이름 검색, 상태별 수, 개별 추가, 엑셀 붙여넣기 등록을 제공한다.
- `/clubs/:clubPublicId/dashboard/members/:memberId`: 회원 상세·수정. 이름, 성별, 출생연도, 티어, 역할, 가입일, 상태와 상태 날짜를 수정한다.
- `/clubs/:clubPublicId/dashboard/members/paste`: TSV 붙여넣기, 행별 미리보기, 오류 수정, 원자적 등록을 제공한다.
- 기존 클럽 상세의 회원 탭은 활동 회원 읽기 전용 명단과 `회원 관리` 이동만 제공한다.

목록에는 이름, 성별, 출생연도, 티어, 가입일, 역할, 상태, 현재 상태 시작일을 표시한다. 휴면·탈퇴 회원은 클럽 접근, 모임 참가, 대진 대상에서 제외한다.

## 엑셀 붙여넣기 등록

- 열 순서는 `이름`, `성별`, `역할`, `티어`, `가입일`, `출생연도`다.
- 가입일·출생연도는 빈값을 허용한다. 가입일은 ISO 날짜, 출생연도는 네 자리 연도여야 한다.
- 입력 내부 중복, 모든 상태의 기존 회원 이름 중복, 날짜·티어·성별 오류가 하나라도 있으면 등록하지 않는다.
- 서버 409 충돌 시 붙여넣기·미리보기 상태를 보존하고 최신 목록 기준의 오류를 표시한다. 부분 등록은 허용하지 않는다.

## API와 권한

- 활동 회원 명단 조회 API는 기존 경로·권한을 유지한다.
- ADMIN 전용 API는 `GET /api/clubs/{clubPublicId}/members/management`, `GET /api/clubs/{clubPublicId}/members/management/{memberId}`, `PATCH /api/clubs/{clubPublicId}/members/{memberId}/status`를 제공한다.
- 기존 개별·일괄 생성과 프로필 수정 API는 가입일·출생연도를 받으며, 생성 시 서버가 상태를 `ACTIVE`로 정한다.
- 휴면·탈퇴·복구는 삭제가 아닌 명시적 상태 변경으로 처리한다.

## 설계 검토

- `memberStatus`가 회원 활성 여부의 단일 기준이다. 기존 `ACTIVE` 열과 그 조회 조건은 이관 뒤 제거한다.
- Controller는 인증 사용자와 HTTP 계약만, application service는 상태 전환·동명이인·마지막 관리자 규칙만, repository는 상태·이름 조건 조회만 담당한다.
- 대시보드 회원 관리가 모든 변경 진입점을 소유하고 클럽 상세는 읽기 전용으로 제한해 UI 정책 중복을 막는다.
