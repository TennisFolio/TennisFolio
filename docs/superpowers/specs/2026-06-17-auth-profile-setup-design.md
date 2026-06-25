# 로그인 후 프로필 설정 설계

## 목표

소셜 로그인 후 `tb_user.NICKNAME` 또는 `tb_user.GENDER`가 비어 있는 사용자는 서비스 이용 전에 이름과 성별을 입력해야 한다.

이 기능은 모바일 웹을 우선으로 한다. 별도 페이지로 이동하지 않고, 로그인 완료 후 현재 화면 위에 하단 시트 형태의 프로필 설정 화면을 표시한다.

## 용어

- 사용자에게 보이는 `nickName` 라벨은 `이름`으로 사용한다.
- 사용자에게 보이는 `gender` 라벨은 `성별`로 사용한다.
- API와 DB 필드는 기존 Java 명명에 맞춰 `nickName`, `gender`를 사용한다.

## 정책

### 이름

- 필수 입력이다.
- 앞뒤 공백은 제거한다.
- 공백만 입력할 수 없다.
- 길이는 1자 이상 10자 이하로 제한한다.
- 중복은 허용한다.

닉네임 중복을 막지 않는 이유는 로그인 식별과 권한 식별이 `userId`와 OAuth 계정으로 처리되기 때문이다. 이름은 화면 표시용이며, 같은 이름을 쓰는 사용자가 자연스럽게 존재할 수 있다.

### 성별

- 필수 입력이다.
- 값은 `MALE`, `FEMALE`만 허용한다.
- 화면에는 `남성`, `여성`으로 표시한다.

성별은 이후 동호회 경기/매칭 기능과 연결될 가능성이 있으므로 기존 matching 도메인의 성별 표현과 맞춰 enum 문자열을 사용한다.

## 서버 응답

`GET /api/auth/me` 응답에 다음 값을 포함한다.

- `userId`
- `email`
- `nickName`
- `gender`
- `needsProfileSetup`

`needsProfileSetup`은 다음 조건 중 하나라도 만족하면 `true`다.

- `nickName == null`
- `nickName.trim().isEmpty()`
- `gender == null`

## 프로필 저장 API

### `PATCH /api/auth/profile`

현재 로그인한 사용자의 이름과 성별을 저장한다.

Request:

```json
{
  "nickName": "민수",
  "gender": "MALE"
}
```

Response:

```json
{
  "code": "0000",
  "message": "성공",
  "data": {
    "userId": 1,
    "email": "user@test.com",
    "nickName": "민수",
    "gender": "MALE",
    "needsProfileSetup": false
  }
}
```

검증 실패 시 `400`을 반환한다.

- 이름이 비어 있음
- 이름이 10자를 초과함
- 성별이 `MALE`, `FEMALE`이 아님

인증되지 않은 요청은 `401`을 반환한다.

## 서버 구조

`User` 도메인 모델과 `UserEntity`에 `gender`를 추가한다.

`AuthQueryService`는 `/api/auth/me` 응답을 만들 때 `needsProfileSetup`을 계산한다.

새 `AuthProfileService`는 다음 책임을 가진다.

- 현재 사용자 조회
- 이름 trim 및 길이 검증
- 성별 enum 검증
- 사용자 프로필 저장
- 갱신된 `AuthMeResponse` 반환

컨트롤러는 repository를 직접 호출하지 않고 `AuthProfileService`만 호출한다.

## 프론트 흐름

1. 앱 부팅 또는 로그인 리다이렉트 후 `/api/auth/me`를 호출한다.
2. 응답의 `needsProfileSetup`이 `true`이면 프로필 설정 하단 시트를 표시한다.
3. 하단 시트는 닫기 버튼을 제공하지 않는다.
4. 사용자는 `이름`과 `성별`을 입력한다.
5. `이름`이 1~10자가 아니거나 성별이 선택되지 않으면 저장 버튼을 비활성화한다.
6. 저장 시 `PATCH /api/auth/profile`을 호출한다.
7. 성공하면 응답의 사용자 정보로 `currentUser`를 갱신하고 시트를 닫는다.

## 화면 구성

하단 시트 제목은 `프로필 설정`이다.

입력 항목:

- `이름`: text input, 최대 10자
- `성별`: `남성`, `여성` 선택 버튼
- 저장 버튼: `저장`

이 화면은 최초 로그인 후 필수 입력 단계이므로, 일반 계정 시트와 달리 닫기 버튼을 제공하지 않는다.

## 테스트

서버 테스트:

- `AuthMeResponse`가 이름 또는 성별 누락 시 `needsProfileSetup=true`를 반환한다.
- `PATCH /api/auth/profile`은 이름과 성별을 저장하고 갱신된 사용자 정보를 반환한다.
- 이름이 비어 있거나 10자를 초과하면 실패한다.
- 성별이 유효하지 않으면 실패한다.

프론트 검증:

- `needsProfileSetup=true`이면 프로필 설정 시트가 표시된다.
- 이름과 성별이 모두 유효해야 저장 버튼이 활성화된다.
- 저장 성공 후 시트가 닫히고 `currentUser`가 갱신된다.
