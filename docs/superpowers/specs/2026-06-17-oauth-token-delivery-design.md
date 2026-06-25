# OAuth 토큰 전달 방식 설계

## 목표

TennisFolio 로그인은 현재 웹 프론트를 대상으로 한다. 모바일 앱이나 외부 API 클라이언트는 이번 범위에서 제외하고, 모바일 브라우저를 포함한 웹 환경을 기준으로 설계한다.

소셜 로그인 제공자는 기존처럼 카카오와 네이버를 사용한다. 서버는 OAuth 콜백을 처리하고, TennisFolio 사용자와 OAuth 계정을 조회하거나 생성한 뒤, TennisFolio JWT를 발급하고 보안 쿠키를 설정한 다음 프론트로 리다이렉트한다.

## 결정 사항

토큰 전달 방식은 `HttpOnly` 쿠키 기반으로 확정한다.

- `access_token`: 짧은 만료 시간을 가진 JWT, `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`
- `refresh_token`: 긴 만료 시간을 가진 JWT, `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`
- `session_id`: refresh token 저장소 조회용 세션 키, `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`

프론트는 토큰 문자열을 직접 읽거나 저장하지 않는다. API 요청에는 `credentials: "include"` 또는 axios `withCredentials: true`를 사용해 쿠키를 포함한다.

## 현재 문제

현재 `OAuthLoginSuccessHandler`는 access token을 `Authorization` 응답 헤더에 넣은 뒤 프론트 URL로 리다이렉트한다. 브라우저 JavaScript는 이 리다이렉트 응답의 헤더를 읽을 수 없으므로, 프론트가 안정적으로 로그인 완료 상태를 만들 수 없다.

반면 refresh token과 session id는 이미 쿠키로 내려가고 있고, `JwtAuthenticationFilter`도 `access_token` 쿠키를 읽는 fallback을 지원한다. 따라서 가장 일관되고 작은 변경은 access token도 쿠키로 전달하도록 바꾸는 것이다.

## 서버 흐름

1. 사용자가 프론트에서 카카오 또는 네이버 로그인 버튼을 누른다.
2. 브라우저가 `/oauth2/authorization/kakao` 또는 `/oauth2/authorization/naver`로 이동한다.
3. Spring Security가 제공자 인증과 사용자 정보 조회를 완료한다.
4. `CustomOAuth2UserService`가 TennisFolio 사용자와 OAuth 계정을 조회하거나 생성한다.
5. `OAuthLoginSuccessHandler`가 access token, refresh token, `session_id`를 발급한다.
6. 서버가 사용자 id와 `session_id` 기준으로 refresh token을 Redis에 저장한다.
7. 서버가 `access_token`, `refresh_token`, `session_id` 쿠키를 설정한다.
8. 서버가 설정된 프론트 URL로 리다이렉트한다.
9. 프론트는 `GET /api/auth/me`를 호출해 로그인 상태와 현재 사용자 정보를 확인한다.

## 인증 API 계약

### `GET /api/auth/me`

현재 인증된 사용자 정보를 반환한다. 토큰 쿠키는 `HttpOnly`라서 프론트가 직접 읽을 수 없으므로, 이 API가 프론트의 로그인 상태 확인 기준이 된다.

인증된 경우 사용자 id와 email을 성공 응답으로 반환한다. 인증되지 않은 경우 `401`을 반환한다.

### `POST /api/auth/reissue`

`refresh_token`과 `session_id` 쿠키를 읽는다. 값이 유효하면 refresh token을 회전시키고, 새 refresh token을 Redis에 저장한 뒤, 새 `refresh_token` 쿠키와 새 `access_token` 쿠키를 설정하고 `204`를 반환한다.

이 API는 access token 만료 후 호출되는 용도이므로 현재 access token 인증을 요구하지 않는다. 대신 refresh token과 `session_id` 쿠키 검증으로 재발급 가능 여부를 판단한다.

### `POST /api/auth/logout`

access token 인증을 요구하지 않는다. 로그아웃은 현재 브라우저의 인증 흔적을 정리하는 종료 API로 취급한다.

서버는 `refresh_token`과 `session_id` 쿠키가 있으면 두 값을 검증해 Redis의 refresh token 항목 삭제를 시도한다. refresh token이 없거나, 만료되었거나, 저장소 삭제에 실패해도 `access_token`, `refresh_token`, `session_id`, `JSESSIONID` 쿠키를 모두 삭제하고 `204`를 반환한다.

### OAuth 연결 해제

OAuth 연결 해제는 path parameter의 `userId`를 신뢰하지 않고, 인증된 principal 기준으로 처리해야 한다. 로그인 흐름에 필요하다면 카카오 연결 해제는 유지할 수 있다. 네이버 연결 해제는 프론트의 계정 삭제 또는 연결 해제 화면이 필요해질 때 별도 범위로 다룬다.

## 보안 설정

인증 관련 엔드포인트는 의도에 맞게 보호한다.

- OAuth authorization/callback 엔드포인트는 공개한다.
- `GET /api/auth/me`는 인증을 요구한다.
- `POST /api/auth/logout`은 공개하되 refresh/session 쿠키가 있으면 서버 세션 삭제를 시도하고, access token이 없어도 쿠키 정리를 위해 `204`를 반환한다.
- `POST /api/auth/reissue`는 공개하되 refresh/session 쿠키를 검증한다.
- 기존 공개 API는 로그인 사용자 기능이 명확히 필요한 경우가 아니면 변경하지 않는다.

CSRF는 이번 범위에서는 기존 프로젝트 설정처럼 비활성화 상태를 유지한다. 다만 쿠키 기반 인증은 CSRF 고려가 더 중요해지므로, 계정 기능이나 인증이 필요한 변경 API가 늘어나기 전에 CSRF 토큰을 추가하거나 인증이 필요한 변경 API의 범위를 좁히는 후속 보안 작업이 필요하다.

## 프론트 흐름

프론트는 카카오/네이버 로그인 진입점을 추가한다. 버튼 클릭 시 백엔드 OAuth authorization URL로 전체 페이지 이동을 수행한다.

OAuth 완료 후 프론트로 돌아오면 앱은 credentials를 포함해 `GET /api/auth/me`를 호출한다. 일반 API 호출 중 `401`을 받으면 `POST /api/auth/reissue`를 호출하고, 성공 시 원래 요청을 한 번만 재시도한다.

로그아웃은 credentials를 포함해 `POST /api/auth/logout`을 호출한 뒤 프론트의 사용자 상태를 비운다.

## 쿠키 정책

웹과 모바일 웹에서는 `SameSite=Lax`를 사용한다. TennisFolio 프론트와 API는 `tennisfolio.net`, `api.tennisfolio.net`처럼 같은 사이트 계열에서 운영될 것으로 보기 때문에, 일반적인 same-site API 요청에는 쿠키가 포함되면서 `SameSite=None`보다 cross-site CSRF 노출을 줄일 수 있다.

운영 환경에서는 `Secure=true`를 사용한다. 로컬 HTTP 개발에서는 Secure 쿠키가 전송되지 않으므로, 환경별 쿠키 설정을 두거나 로컬 HTTPS 개발 환경을 사용하는 선택이 필요하다.

## 테스트

서버 테스트는 다음을 확인한다.

- OAuth 성공 시 세 쿠키가 모두 설정되고 프론트 URL로 리다이렉트된다.
- 재발급 시 refresh token이 회전되고 새 access/refresh 쿠키가 설정된다.
- 로그아웃 시 모든 인증 쿠키가 삭제되고 Redis refresh token 항목이 삭제된다.
- `/api/auth/me`는 인증된 사용자 정보를 반환하고, 미인증 요청에는 `401`을 반환한다.

프론트 검증은 다음을 확인한다.

- 로그인 버튼이 올바른 백엔드 OAuth authorization URL로 이동한다.
- 앱 초기 진입 시 credentials를 포함해 `/api/auth/me`를 호출한다.
- access token 만료 시 재발급 후 원래 요청을 한 번 재시도한다.
- 로그아웃 후 화면의 로그인 상태가 해제된다.
