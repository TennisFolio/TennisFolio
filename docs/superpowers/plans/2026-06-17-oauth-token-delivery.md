# OAuth Token Delivery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 카카오/네이버 OAuth 로그인 성공 후 웹/모바일 웹 프론트가 HttpOnly 쿠키 기반으로 로그인 상태를 사용할 수 있게 만든다.

**Architecture:** 서버는 OAuth 성공 시 `access_token`, `refresh_token`, `session_id`를 모두 보안 쿠키로 내려주고 프론트로 리다이렉트한다. 프론트는 토큰 값을 직접 읽지 않고 `GET /api/auth/me`, `POST /api/auth/reissue`, `POST /api/auth/logout` API와 credentials 포함 요청으로 인증 상태를 관리한다.

**Tech Stack:** Spring Boot 3, Spring Security OAuth2 Client, JWT, Redis, JUnit 5, Mockito, React 19, Vite, axios

**Commit Policy:** 사용자 요청에 따라 이 계획 실행 중에는 커밋하지 않는다.

---

## 파일 구조

- Modify: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/handler/OAuthLoginSuccessHandler.java`
  - OAuth 성공 시 access token을 헤더가 아니라 쿠키로 전달한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/util/CookieUtils.java`
  - 인증 쿠키 생성/삭제 공통 로직을 제공한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/service/ReIssueService.java`
  - 재발급 결과로 새 access token과 새 refresh token을 함께 반환한다.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/dto/ReissuedToken.java`
  - 재발급 결과 DTO.
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthMeResponse.java`
  - `/api/auth/me` 응답 DTO.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
  - `me`, `reissue`, `logout` 쿠키 기반 계약을 구현한다.
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
  - 인증이 필요한 `/api/auth/me`, `/api/auth/logout`만 보호하고 `/api/auth/reissue`는 refresh 쿠키 검증에 맡긴다.
- Modify: `src/test/java/com/tennisfolio/Tennisfolio/security/oauth/handler/OAuthLoginSuccessHandlerTest.java`
  - OAuth 성공 시 세 쿠키가 설정되는지 검증한다.
- Create: `src/test/java/com/tennisfolio/Tennisfolio/security/oauth/service/ReIssueServiceTest.java`
  - refresh token 회전 결과를 검증한다.
- Create: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`
  - `me`, `reissue`, `logout` API 동작을 검증한다.
- Modify: `src/tennisFolio/src/constants/urls.js`
  - OAuth authorization URL 상수를 추가한다.
- Modify: `src/tennisFolio/src/utils/apiClient.js`
  - axios credentials 포함과 401 재발급 후 1회 재시도를 추가한다.
- Create: `src/tennisFolio/src/utils/authApi.js`
  - 프론트 인증 API 래퍼와 소셜 로그인 이동 함수를 제공한다.

---

### Task 1: 인증 쿠키 공통 유틸과 OAuth 성공 쿠키 전환

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/util/CookieUtils.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/handler/OAuthLoginSuccessHandler.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/security/oauth/handler/OAuthLoginSuccessHandlerTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`OAuthLoginSuccessHandlerTest`의 기존 테스트를 다음 기대값으로 바꾼다.

```java
@Test
void oauthSuccess_setsAuthCookiesAndRedirects() throws Exception {
    Long userId = 1L;
    CustomOAuth2User principal = CustomOAuth2User.builder()
            .userId(userId)
            .build();
    Authentication authentication =
            new UsernamePasswordAuthenticationToken(principal, null, List.of());

    when(jwtTokenProvider.createAccessToken(userId)).thenReturn("access-token");
    when(jwtTokenProvider.createRefreshToken(userId)).thenReturn("refresh-token");

    MockHttpServletResponse response = new MockHttpServletResponse();

    successHandler.onAuthenticationSuccess(
            new MockHttpServletRequest(),
            response,
            authentication
    );

    verify(jwtTokenProvider).createAccessToken(userId);
    verify(jwtTokenProvider).createRefreshToken(userId);
    verify(refreshTokenService).save(eq(userId), anyString(), eq("refresh-token"));

    assertThat(response.getHeader("Authorization")).isNull();

    Cookie[] cookies = response.getCookies();
    assertThat(cookies).hasSize(3);
    assertThat(cookie(cookies, "access_token").getValue()).isEqualTo("access-token");
    assertThat(cookie(cookies, "refresh_token").getValue()).isEqualTo("refresh-token");
    assertThat(cookie(cookies, "session_id").getValue()).isNotBlank();

    assertThat(cookie(cookies, "access_token").isHttpOnly()).isTrue();
    assertThat(cookie(cookies, "access_token").getSecure()).isTrue();
    assertThat(cookie(cookies, "access_token").getPath()).isEqualTo("/");
    assertThat(cookie(cookies, "refresh_token").getMaxAge()).isEqualTo(60 * 60 * 24 * 14);

    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:4173");
}

private static Cookie cookie(Cookie[] cookies, String name) {
    return Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst()
            .orElseThrow();
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandlerTest"
```

Expected: `Authorization` 헤더가 아직 존재하고 `access_token` 쿠키가 없어 실패한다.

- [ ] **Step 3: `CookieUtils`에 인증 쿠키 생성 메서드 추가**

`CookieUtils`를 다음 형태로 확장한다.

```java
public static Cookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    return cookie;
}
```

기존 `deleteCookie`는 그대로 두되, 삭제 쿠키도 동일한 속성을 유지한다.

- [ ] **Step 4: `OAuthLoginSuccessHandler` 구현 변경**

`applyTokenToResponse`에서 `Authorization` 헤더 설정을 제거하고 세 쿠키를 추가한다.

```java
private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 60 * 30;
private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 60 * 60 * 24 * 14;

private void applyTokenToResponse(HttpServletResponse response, LoginToken token) {
    response.addCookie(createHttpOnlyCookie(
            "access_token",
            token.getAccessToken(),
            ACCESS_TOKEN_MAX_AGE_SECONDS
    ));
    response.addCookie(createHttpOnlyCookie(
            "refresh_token",
            token.getRefreshToken(),
            REFRESH_TOKEN_MAX_AGE_SECONDS
    ));
    response.addCookie(createHttpOnlyCookie(
            "session_id",
            token.getSessionId(),
            REFRESH_TOKEN_MAX_AGE_SECONDS
    ));
    response.setStatus(HttpServletResponse.SC_OK);
}
```

`CookieUtils.createHttpOnlyCookie`를 static import한다. 기존 private cookie 생성 메서드는 제거한다.

- [ ] **Step 5: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandlerTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 2: refresh 재발급 결과에 새 refresh token 포함

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/dto/ReissuedToken.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/security/oauth/service/ReIssueService.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/security/oauth/service/ReIssueServiceTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`ReIssueServiceTest`를 새로 만든다.

```java
package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.dto.ReissuedToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReIssueServiceTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    ReIssueService reIssueService;

    @Test
    void reIssue_rotatesRefreshTokenAndReturnsBothTokens() {
        String oldRefreshToken = "old-refresh";
        String sessionId = "session-1";
        Long userId = 1L;

        when(jwtTokenProvider.getUserId(oldRefreshToken)).thenReturn(userId);
        when(refreshTokenService.get(userId, sessionId)).thenReturn(oldRefreshToken);
        when(jwtTokenProvider.createRefreshToken(userId)).thenReturn("new-refresh");
        when(jwtTokenProvider.createAccessToken(userId)).thenReturn("new-access");

        ReissuedToken result = reIssueService.reIssue(oldRefreshToken, sessionId);

        verify(jwtTokenProvider).validateOrThrow(oldRefreshToken);
        verify(refreshTokenService).save(userId, sessionId, "new-refresh");
        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
    }
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueServiceTest"
```

Expected: `ReissuedToken` 클래스가 없거나 `reIssue` 반환 타입이 맞지 않아 실패한다.

- [ ] **Step 3: DTO 생성**

`ReissuedToken.java`를 만든다.

```java
package com.tennisfolio.Tennisfolio.security.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissuedToken {
    private String accessToken;
    private String refreshToken;
}
```

- [ ] **Step 4: `ReIssueService` 반환 타입 변경**

`reIssue` 메서드를 다음 형태로 바꾼다.

```java
public ReissuedToken reIssue(String refreshToken, String sessionId) {
    jwtTokenProvider.validateOrThrow(refreshToken);

    Long userId = jwtTokenProvider.getUserId(refreshToken);

    String saved = refreshTokenService.get(userId, sessionId);
    if (saved == null || !saved.equals(refreshToken)) {
        throw new RuntimeException("Invalid refresh token");
    }

    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
    refreshTokenService.save(userId, sessionId, newRefreshToken);

    String newAccessToken = jwtTokenProvider.createAccessToken(userId);
    return new ReissuedToken(newAccessToken, newRefreshToken);
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueServiceTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 3: AuthController 쿠키 기반 me/reissue/logout 구현

**Files:**
- Create: `src/main/java/com/tennisfolio/Tennisfolio/user/dto/AuthMeResponse.java`
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/user/api/AuthController.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`

- [ ] **Step 1: 실패 테스트 작성**

`AuthControllerTest`를 새로 만든다.

```java
package com.tennisfolio.Tennisfolio.user.api;

import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.security.oauth.dto.ReissuedToken;
import com.tennisfolio.Tennisfolio.security.oauth.service.OAuthUnlinkService;
import com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueService;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    OAuthUnlinkService oAuthUnlinkService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    ReIssueService reIssueService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthController authController;

    @Test
    void me_returnsCurrentUser() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(1L, null, List.of());
        User user = User.builder()
                .userId(1L)
                .email("user@test.com")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.me(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void reIssue_setsNewAccessAndRefreshCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("refresh_token", "old-refresh"),
                new Cookie("session_id", "session-1")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(reIssueService.reIssue("old-refresh", "session-1"))
                .thenReturn(new ReissuedToken("new-access", "new-refresh"));

        ResponseEntity<Void> result = authController.reIssue(request, response);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(cookie(response.getCookies(), "access_token").getValue()).isEqualTo("new-access");
        assertThat(cookie(response.getCookies(), "refresh_token").getValue()).isEqualTo("new-refresh");
        assertThat(response.getHeader("Authorization")).isNull();
    }

    @Test
    void logout_deletesRefreshTokenAndClearsAllAuthCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("session_id", "session-1"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(1L, null, List.of());

        ResponseEntity<Void> result = authController.logout(request, response, authentication);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        verify(refreshTokenService).delete(1L, "session-1");
        assertThat(cookie(response.getCookies(), "access_token").getMaxAge()).isZero();
        assertThat(cookie(response.getCookies(), "refresh_token").getMaxAge()).isZero();
        assertThat(cookie(response.getCookies(), "session_id").getMaxAge()).isZero();
    }

    private static Cookie cookie(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        throw new AssertionError("cookie not found: " + name);
    }
}
```

- [ ] **Step 2: 실패 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `me` 메서드와 `UserRepository` 의존성 또는 재발급 반환 타입 불일치로 실패한다.

- [ ] **Step 3: `AuthMeResponse` 생성**

```java
package com.tennisfolio.Tennisfolio.user.dto;

import com.tennisfolio.Tennisfolio.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMeResponse {
    private Long userId;
    private String email;
    private String nickName;

    public static AuthMeResponse from(User user) {
        return new AuthMeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickName()
        );
    }
}
```

- [ ] **Step 4: `AuthController` 생성자와 `me` 구현**

`UserRepository`를 주입하고 메서드를 추가한다.

```java
@GetMapping("/me")
public ResponseEntity<ResponseDTO<AuthMeResponse>> me(Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return ResponseEntity.ok(ResponseDTO.success(AuthMeResponse.from(user)));
}
```

- [ ] **Step 5: `reIssue` 응답을 쿠키 방식으로 변경**

```java
@PostMapping("/reissue")
public ResponseEntity<Void> reIssue(
        HttpServletRequest request,
        HttpServletResponse response
) {
    String sessionId = getCookie(request, "session_id");
    String refreshToken = getCookie(request, "refresh_token");
    ReissuedToken token = reIssueService.reIssue(refreshToken, sessionId);

    response.addCookie(createHttpOnlyCookie("access_token", token.getAccessToken(), 60 * 30));
    response.addCookie(createHttpOnlyCookie("refresh_token", token.getRefreshToken(), 60 * 60 * 24 * 14));

    return ResponseEntity.noContent().build();
}
```

기존 `/reIssue` 대소문자는 프론트 신규 구현에서 `/reissue`로 사용한다. 호환성이 필요하면 같은 메서드에 `@PostMapping({"/reissue", "/reIssue"})`를 사용한다.

- [ ] **Step 6: `logout`에서 access 쿠키도 삭제**

```java
deleteCookie(response, "access_token");
deleteCookie(response, "refresh_token");
deleteCookie(response, "session_id");
```

`System.out.println` 로그는 제거한다.

- [ ] **Step 7: 테스트 통과 확인**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 4: SecurityConfig 인증 경로 정리

**Files:**
- Modify: `src/main/java/com/tennisfolio/Tennisfolio/config/SecurityConfig.java`
- Test: `src/test/java/com/tennisfolio/Tennisfolio/user/api/AuthControllerTest.java`

- [ ] **Step 1: 보안 규칙 변경**

`authorizeHttpRequests`를 다음 의도로 바꾼다.

```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
        .requestMatchers("/api/auth/reissue", "/api/auth/reIssue").permitAll()
        .anyRequest().permitAll()
)
```

- [ ] **Step 2: 컴파일 확인**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: 인증 컨트롤러 테스트 재실행**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `BUILD SUCCESSFUL`.

---

### Task 5: 프론트 API 클라이언트 credentials와 reissue 재시도

**Files:**
- Modify: `src/tennisFolio/src/utils/apiClient.js`
- Modify: `src/tennisFolio/src/constants/urls.js`
- Create: `src/tennisFolio/src/utils/authApi.js`

- [ ] **Step 1: URL 상수 추가**

`constants/urls.js`에 OAuth authorization URL 생성 함수를 추가한다.

```javascript
export const oauth_authorization_urls = {
  kakao: `${base_server_url}/oauth2/authorization/kakao`,
  naver: `${base_server_url}/oauth2/authorization/naver`,
};
```

- [ ] **Step 2: 인증 API 래퍼 생성**

`authApi.js`를 만든다.

```javascript
import { oauth_authorization_urls } from '@/constants';
import { apiRequestSilent } from './apiClient';

export const loginWithProvider = (provider) => {
  const url = oauth_authorization_urls[provider];
  if (!url) {
    throw new Error(`Unsupported OAuth provider: ${provider}`);
  }
  window.location.assign(url);
};

export const getCurrentUser = () =>
  apiRequestSilent.get('/api/auth/me');

export const reissueAuthTokens = () =>
  apiRequestSilent.post('/api/auth/reissue');

export const logout = () =>
  apiRequestSilent.post('/api/auth/logout');
```

- [ ] **Step 3: axios credentials 기본값 추가**

`apiClient` 생성부에 `withCredentials: true`를 추가한다.

```javascript
const apiClient = axios.create({
  baseURL: '',
  timeout: 20000,
  withCredentials: true,
});
```

- [ ] **Step 4: 401 재발급 후 1회 재시도 구현**

response error interceptor에서 기존 hideLoading 처리 후 다음 로직을 추가한다. `/api/auth/reissue` 자체는 재시도하지 않는다.

```javascript
const originalRequest = error.config;
const status = error.response?.status;
const url = originalRequest?.url || '';

if (
  status === 401 &&
  originalRequest &&
  !originalRequest._authRetry &&
  !url.includes('/api/auth/reissue')
) {
  originalRequest._authRetry = true;
  try {
    await apiClient.post('/api/auth/reissue', {}, { showLoading: false });
    return apiClient(originalRequest);
  } catch (reissueError) {
    return Promise.reject(reissueError);
  }
}
```

이 interceptor 함수는 `async (error) => { ... }` 형태여야 한다.

- [ ] **Step 5: 프론트 빌드 확인**

Run:

```powershell
npm run build
```

Workdir:

```text
src/tennisFolio
```

Expected: Vite build succeeds.

---

### Task 6: 로그인 진입점과 앱 부팅 시 me 확인

**Files:**
- Modify: `src/tennisFolio/src/App.jsx`
- Modify: an existing visible layout/navigation component after locating it, likely `src/tennisFolio/src/Layout.jsx`
- Use: `src/tennisFolio/src/utils/authApi.js`

- [ ] **Step 1: 레이아웃 파일 위치 확인**

Run:

```powershell
rg -n "function Layout|const Layout|export default Layout|nav|header|로그인|login" src\tennisFolio\src -S
```

Expected: 로그인 버튼을 둘 위치가 되는 `Layout.jsx` 또는 관련 header 파일을 찾는다.

- [ ] **Step 2: 앱 부팅 시 `/api/auth/me` 호출**

`App.jsx`에 최소 상태를 둔다.

```javascript
import { useEffect, useState } from 'react';
import { getCurrentUser } from './utils/authApi';
```

`App` 내부에 추가한다.

```javascript
const [currentUser, setCurrentUser] = useState(null);

useEffect(() => {
  let cancelled = false;

  getCurrentUser()
    .then((response) => {
      if (!cancelled) {
        setCurrentUser(response.data.data);
      }
    })
    .catch(() => {
      if (!cancelled) {
        setCurrentUser(null);
      }
    });

  return () => {
    cancelled = true;
  };
}, []);
```

`Layout`에 props 전달이 가능하면 다음처럼 전달한다.

```jsx
<Layout currentUser={currentUser} onLogout={() => setCurrentUser(null)}>
```

- [ ] **Step 3: 로그인 버튼 추가**

레이아웃/header 컴포넌트에 다음 핸들러를 연결한다.

```javascript
import { loginWithProvider, logout } from './utils/authApi';

const handleKakaoLogin = () => loginWithProvider('kakao');
const handleNaverLogin = () => loginWithProvider('naver');
const handleLogout = async () => {
  await logout();
  onLogout?.();
};
```

UI는 기존 header 스타일을 따른다. 최소 동작 버튼은 다음 형태다.

```jsx
{currentUser ? (
  <button type="button" onClick={handleLogout}>로그아웃</button>
) : (
  <>
    <button type="button" onClick={handleKakaoLogin}>카카오 로그인</button>
    <button type="button" onClick={handleNaverLogin}>네이버 로그인</button>
  </>
)}
```

- [ ] **Step 4: 프론트 빌드 확인**

Run:

```powershell
npm run build
```

Workdir:

```text
src/tennisFolio
```

Expected: Vite build succeeds.

---

### Task 7: 전체 검증

**Files:**
- No direct edits.

- [ ] **Step 1: 백엔드 컴파일**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: 관련 백엔드 테스트**

Run:

```powershell
.\gradlew.bat test --tests "com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandlerTest" --tests "com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueServiceTest" --tests "com.tennisfolio.Tennisfolio.user.api.AuthControllerTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: 프론트 빌드**

Run:

```powershell
npm run build
```

Workdir:

```text
src/tennisFolio
```

Expected: Vite build succeeds.

- [ ] **Step 4: 수동 확인**

로컬 또는 개발 서버에서 다음을 확인한다.

1. 카카오 로그인 버튼 클릭 시 `/oauth2/authorization/kakao`로 이동한다.
2. 네이버 로그인 버튼 클릭 시 `/oauth2/authorization/naver`로 이동한다.
3. OAuth 성공 후 프론트로 돌아왔을 때 브라우저 devtools Application 탭에 `access_token`, `refresh_token`, `session_id` 쿠키가 존재한다.
4. `/api/auth/me` 응답이 현재 사용자 정보를 반환한다.
5. 로그아웃 후 세 쿠키가 삭제되고 화면이 로그아웃 상태가 된다.

---

## 자체 검토

- 설계의 핵심 결정인 HttpOnly 쿠키 기반 전달은 Task 1, Task 3, Task 5에서 구현된다.
- refresh token 회전과 새 쿠키 반영은 Task 2, Task 3에서 구현된다.
- `/api/auth/me`는 Task 3, 프론트 부팅 확인은 Task 6에서 구현된다.
- 보안 경로 정리는 Task 4에서 구현된다.
- 프론트 로그인 진입점과 credentials 포함 요청은 Task 5, Task 6에서 구현된다.
- 사용자 요청에 따라 커밋 단계는 제외했다.
