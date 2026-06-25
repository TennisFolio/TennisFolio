# Competition 계정 소유자 연결 설계

## 목표

로그인한 사용자가 Competition을 생성하면 해당 Competition을 사용자 계정에 연결한다. 이후 사용자가 다시 로그인했을 때 자신이 생성한 Competition 목록을 조회할 수 있어야 한다.

비로그인 Competition 생성은 현재처럼 계속 동작해야 한다.

## 범위

이 설계는 1차 버전만 다룬다.

- `POST /api/competitions`는 비로그인 사용자에게도 계속 허용한다.
- 요청에 유효한 인증 정보가 있으면 생성된 Competition을 현재 사용자에게 자동 연결한다.
- 현재 로그인 사용자의 Competition 목록을 조회하는 인증 API를 추가한다.
- 기존 Competition 관리자 토큰 흐름은 변경하지 않는다.

비로그인으로 생성한 Competition을 로그인 후 가져오는 기능은 이번 범위에 포함하지 않는다. 이 기능은 나중에 Competition 관리자 토큰 또는 관리자 비밀번호를 소유 증명으로 사용하는 별도 흐름으로 추가할 수 있다.

## 데이터 모델

`tb_competition`에 nullable 소유자 컬럼을 추가한다.

```text
tb_competition
- COMPETITION_ID
- PUBLIC_ID
- ...
- OWNER_USER_ID nullable FK -> tb_user.USER_ID
```

의미는 다음과 같다.

- `OWNER_USER_ID IS NULL`: 비로그인 상태에서 생성된 Competition
- `OWNER_USER_ID = user_id`: 해당 로그인 사용자가 생성한 Competition

1차 버전은 단일 소유자 모델을 사용한다. 하나의 Competition은 최대 한 계정만 소유할 수 있다.

연결 테이블보다 이 방식이 현재 요구사항에 더 단순하다. 추후 공동 운영자, 협업자, 즐겨찾기, 소유와 별개인 저장 목록이 필요해지면 그때 연결 테이블을 도입한다.

## 백엔드 동작

`POST /api/competitions`는 공개 엔드포인트로 유지한다.

요청이 인증된 상태라면 `CompetitionController`가 `Authentication.getPrincipal()`에서 현재 `userId`를 읽고 생성 서비스로 전달한다. 생성 서비스는 이 값을 `Competition.ownerUserId`에 저장한다.

요청이 인증되지 않았다면 기존 생성 흐름을 그대로 유지하고 `Competition.ownerUserId`는 `null`로 저장한다.

기존 생성 응답은 클라이언트 이동과 수정 권한에 필요한 값만 유지한다.

```json
{
  "publicId": "...",
  "competitionAdminToken": "..."
}
```

이번 버전에서 Competition 수정 권한의 기준은 계속 관리자 토큰이다. 계정 소유자는 목록 조회를 위한 연결 정보이며, 새로운 수정 권한으로 사용하지 않는다.

## 현재 사용자 Competition 목록 API

다음 API를 추가한다.

```http
GET /api/auth/me/competitions
```

이 API는 인증이 필요하다. 현재 인증된 사용자의 `userId`와 `ownerUserId`가 일치하는 Competition만 반환한다.

초기 응답 형태는 다음과 같다.

```json
[
  {
    "publicId": "...",
    "name": "토요일 복식 모임",
    "maleCount": 8,
    "femaleCount": 4,
    "courtCount": 3,
    "rounds": 5,
    "status": "READY",
    "mode": "FIXED_SCHEDULE",
    "createdAt": "2026-06-22T12:30:00"
  }
]
```

목록은 최신 생성 Competition이 먼저 오도록 정렬한다.

## 프론트엔드 동작

Competition 생성 폼의 요청 payload는 변경하지 않는다. 기존 API 클라이언트가 인증 쿠키를 함께 전송하므로, 로그인 상태 여부는 백엔드에서 판단할 수 있다.

`내 Competition` 화면 또는 섹션은 로그인 후 `GET /api/auth/me/competitions`를 호출해 목록을 보여준다. 목록에서 항목을 선택하면 기존 Competition 상세 라우트로 `publicId`를 사용해 이동한다.

비로그인 사용자는 기존 링크와 관리자 토큰 기반 경험을 그대로 사용한다.

## 예외 처리

- 비로그인 `POST /api/competitions`: 성공하며 소유자 없는 Competition을 생성한다.
- 로그인 `POST /api/competitions`: 성공하며 현재 사용자 소유 Competition을 생성한다.
- 인증 없이 `GET /api/auth/me/competitions` 호출: `401`을 반환한다.
- 다른 사용자가 소유한 Competition은 현재 사용자의 목록에 절대 포함하지 않는다.
- 기존 공개 상세/결과 조회 API는 `publicId` 기준으로 계속 동작한다.

## 추후 확장

필요해지면 비로그인 Competition 가져오기 흐름을 추가한다.

가능한 추후 API:

```http
POST /api/auth/me/competitions/claims
```

요청은 `publicId`와 소유 증명 값을 함께 보낸다. 소유 증명은 `competitionAdminToken` 또는 관리자 비밀번호가 될 수 있다. 백엔드는 해당 Competition에 아직 소유자가 없고 증명이 유효할 때만 현재 사용자에게 연결한다.

이 기능은 초기 소유자 연결 기능을 작고 안정적으로 유지하기 위해 1차 범위에서 제외한다.

## 테스트 범위

백엔드 테스트는 다음을 검증한다.

- 비로그인 Competition 생성 시 소유자가 저장되지 않는다.
- 로그인 Competition 생성 시 현재 사용자 id가 소유자로 저장된다.
- 현재 사용자 Competition 목록은 해당 사용자의 Competition만 반환한다.
- 현재 사용자 Competition 목록은 비로그인 Competition과 다른 사용자 Competition을 제외한다.
- 현재 사용자 Competition 목록은 인증이 필요하다.

프론트엔드 테스트는 목록 UI 진입점이 추가될 때 목록 렌더링을 검증한다.
