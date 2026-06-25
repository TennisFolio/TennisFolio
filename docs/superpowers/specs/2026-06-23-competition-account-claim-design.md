# Competition 계정 저장 설계

## 목표

비로그인 상태에서 만든 Competition을 나중에 로그인 계정에 저장할 수 있게 한다. 저장된 Competition은 기존 `내 경기` 화면에서 다시 찾고 관리할 수 있어야 한다.

이 기능은 공동 소유자 모델이 아니다. 현재 데이터 모델은 `tb_competition.OWNER_USER_ID` 하나만 사용하므로, 한 Competition은 최대 한 계정에만 저장된다.

## 범위

이번 범위에 포함한다.

- 대진표/운영 화면에서 `내 계정에 저장` 진입점을 제공한다.
- 로그인 사용자가 운영 권한을 증명하면 `ownerUserId`가 비어 있는 Competition을 자신의 계정에 저장할 수 있다.
- 저장 버튼에서 시작한 로그인은 로그인 완료 후 원래 Competition 화면으로 돌아와 자동 저장을 시도한다.
- 헤더의 일반 로그인은 자동 저장하지 않는다.
- 이미 다른 계정에 저장된 Competition은 추가 저장을 막는다.

이번 범위에서 제외한다.

- 여러 계정이 같은 Competition을 저장하는 공동 소유자/공동 관리자 모델
- 소유권 이전
- 저장 취소
- 다른 계정에 저장된 Competition의 소유자 정보 노출
- 삭제된 Competition 복구

## 용어

- 계정 저장: `Competition.ownerUserId`에 현재 로그인 사용자의 `userId`를 저장하는 행위
- claim: 개발 내부 용어로, 운영 권한을 증명한 사용자가 아직 소유자가 없는 Competition을 자기 계정에 귀속시키는 행위
- `claimAfterLogin=1`: 로그인 완료 후 계정 저장을 이어서 실행해야 한다는 URL 의도 플래그

사용자에게는 `claim`이라는 단어를 노출하지 않는다. 화면 문구는 `내 계정에 저장`을 사용한다.

## 저장 가능 조건

아래 조건을 모두 만족할 때만 저장할 수 있다.

```text
Competition.deletedAt == null
Competition.ownerUserId == null 또는 현재 사용자 id
현재 사용자가 로그인 상태
현재 브라우저가 유효한 editToken 또는 adminToken을 보유
```

`editToken`은 Competition 생성 직후 발급된 관리자 토큰이고, `adminToken`은 운영 비밀번호 로그인으로 발급된 관리자 토큰이다. 둘 다 기존 운영 API에서 사용하는 `X-Competition-Admin-Token` 헤더로 전달한다.

다른 사용자가 이미 저장한 Competition이면 저장 API는 `409 Conflict`를 반환한다.

## 화면 노출 정책

대진표/운영 화면은 상세 API 응답과 로컬 관리자 토큰 보유 여부를 기준으로 `내 계정에 저장` 배너 또는 버튼을 보여준다.

노출 조건:

```text
ownerUserId 없음
+ 현재 브라우저가 editToken 또는 adminToken 보유
```

비노출 조건:

```text
ownerUserId가 있음
관리자 토큰이 없음
삭제된 Competition
```

이미 현재 사용자의 계정에 저장된 상태를 알려줄 필요가 있으면 버튼 대신 짧은 상태 문구를 사용한다.

```text
내 계정에 저장된 경기입니다.
```

## 사용자 흐름

### 로그인 상태에서 저장

```text
대진표/운영 화면
-> 내 계정에 저장 클릭
-> POST /api/auth/me/competitions/{publicId}/claim 호출
-> 성공 메시지 표시
-> 내 경기 목록에 포함됨
```

성공 문구:

```text
내 계정에 저장했어요.
```

### 비로그인 상태에서 저장

```text
대진표/운영 화면
-> 내 계정에 저장 클릭
-> 로그인 화면 또는 OAuth 로그인으로 이동
-> 로그인 성공
-> /competitions/{publicId}?claimAfterLogin=1 로 복귀
-> 화면이 claimAfterLogin=1 감지
-> POST /api/auth/me/competitions/{publicId}/claim 호출
-> 성공/실패 메시지 표시
-> URL에서 claimAfterLogin 제거
```

`claimAfterLogin=1`은 사용자 의도 플래그일 뿐이다. 실제 저장 권한은 서버가 로그인 사용자와 관리자 토큰으로 다시 검증한다.

### 헤더 로그인

헤더의 로그인 버튼으로 로그인한 경우에는 자동 저장하지 않는다.

```text
대진표/운영 화면
-> 헤더 로그인 클릭
-> 로그인 성공
-> 원래 화면 복귀
-> 저장 가능 조건이면 내 계정에 저장 버튼을 계속 표시
```

소유권 저장은 사용자가 `내 계정에 저장` 버튼을 직접 눌렀을 때만 발생한다.

## API

```http
POST /api/auth/me/competitions/{publicId}/claim
X-Competition-Admin-Token: {editToken 또는 adminToken}
```

인증이 필요하다.

성공 응답은 본문 없이 처리한다.

```http
204 No Content
```

서버 처리 순서:

```text
1. 현재 로그인 사용자 id를 확인한다.
2. publicId로 삭제되지 않은 Competition을 조회한다.
3. X-Competition-Admin-Token을 검증한다.
4. ownerUserId가 null이면 현재 사용자 id를 저장한다.
5. ownerUserId가 현재 사용자 id이면 멱등 성공으로 처리한다.
6. ownerUserId가 다른 사용자 id이면 409 Conflict를 반환한다.
```

에러 정책:

- 비로그인 요청: `401`
- 존재하지 않거나 삭제된 Competition: `404`
- 관리자 토큰 누락 또는 유효하지 않음: `401` 또는 기존 관리자 토큰 검증 실패 응답을 따른다.
- 이미 다른 계정에 저장된 Competition: `409`

409 응답의 사용자 문구:

```text
이미 다른 계정에 저장된 경기입니다.
```

## 프론트엔드 URL 처리

저장 버튼에서 로그인으로 이동할 때는 현재 Competition 화면을 redirect 대상으로 유지하고 `claimAfterLogin=1`을 붙인다.

예:

```text
/competitions/{publicId}?claimAfterLogin=1
```

로그인 완료 후 Competition 화면은 query를 확인한다.

- `claimAfterLogin=1`이 있고 로그인 상태이면 claim API를 한 번 호출한다.
- 호출 후 성공/실패와 관계없이 URL에서 `claimAfterLogin`을 제거한다.
- 새로고침으로 같은 claim API가 반복 호출되지 않아야 한다.
- 헤더 로그인 흐름에는 `claimAfterLogin=1`을 붙이지 않는다.

## 상세 API 응답 필요값

대진표/운영 화면이 저장 버튼 표시 여부를 판단하려면 Competition 상세 응답에 소유 상태를 판단할 수 있는 값이 필요하다.

권장 값:

```json
{
  "ownerUserIdSet": true
}
```

현재 사용자가 소유자인지까지 화면에서 구분해야 한다면 인증 선택형 API 또는 별도 `GET /api/auth/me` 결과와 조합해야 한다. 이번 기능의 기본 노출은 `ownerUserId` 존재 여부와 관리자 토큰 보유 여부만으로 충분하다.

## 데이터 모델

새 테이블을 추가하지 않는다.

기존 `tb_competition.OWNER_USER_ID`를 사용한다.

```text
OWNER_USER_ID = null
-> 아직 어떤 계정에도 저장되지 않은 Competition

OWNER_USER_ID = currentUserId
-> 현재 사용자의 내 경기 목록에 표시되는 Competition

OWNER_USER_ID = anotherUserId
-> 이미 다른 계정에 저장된 Competition
```

공동 저장 또는 공동 운영이 필요해지면 `tb_competition_owner` 같은 연결 테이블을 별도 기능으로 검토한다.

## 테스트 범위

백엔드 테스트:

- 로그인 사용자가 유효한 관리자 토큰으로 owner 없는 Competition을 저장할 수 있다.
- 이미 본인 계정에 저장된 Competition 저장 요청은 성공으로 처리한다.
- 다른 계정에 저장된 Competition 저장 요청은 `409`로 처리한다.
- 관리자 토큰이 없거나 유효하지 않으면 저장하지 않는다.
- 삭제된 Competition은 저장할 수 없다.
- 비로그인 요청은 인증 오류로 처리한다.

프론트엔드 테스트:

- 저장 가능 조건에서 `내 계정에 저장` 버튼을 표시한다.
- 관리자 토큰이 없으면 버튼을 표시하지 않는다.
- 저장 버튼 클릭 시 로그인 상태면 claim API를 호출한다.
- 비로그인 상태에서 저장 버튼 클릭 시 `claimAfterLogin=1`이 포함된 로그인 흐름으로 이동한다.
- `claimAfterLogin=1`로 돌아온 뒤 claim API를 한 번 호출하고 query를 제거한다.
- 헤더 로그인으로 돌아온 경우에는 claim API를 호출하지 않는다.
- 409 응답이면 `이미 다른 계정에 저장된 경기입니다.`를 표시한다.

## 구현 메모

기존 스펙 `2026-06-22-competition-owner-account-design.md`에서는 비로그인 Competition 가져오기를 1차 범위에서 제외했다. 이 문서는 그 후속 기능으로, 같은 단일 `ownerUserId` 모델을 유지하면서 계정 저장 흐름만 추가한다.

`ENTITY_DESIGN.md`는 현재 워크스페이스에서 찾을 수 없었다. 구현 시 파일이 복구되거나 경로가 확인되면 해당 문서의 aggregate 규칙을 다시 확인한다.
