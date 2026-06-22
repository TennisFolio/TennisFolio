package com.tennisfolio.Tennisfolio.user.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.security.oauth.dto.ReissuedToken;
import com.tennisfolio.Tennisfolio.security.oauth.service.OAuthUnlinkService;
import com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueService;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.dto.AuthProfileUpdateRequest;
import com.tennisfolio.Tennisfolio.user.service.AuthLogoutService;
import com.tennisfolio.Tennisfolio.user.service.AuthProfileService;
import com.tennisfolio.Tennisfolio.user.service.AuthQueryService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
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
    AuthQueryService authQueryService;

    @Mock
    AuthLogoutService authLogoutService;

    @Mock
    AuthProfileService authProfileService;

    @InjectMocks
    AuthController authController;

    @Test
    void me_returnsCurrentUser() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(1L, null, List.of());
        when(authQueryService.getCurrentUser(1L))
                .thenReturn(new AuthMeResponse(1L, "user@test.com", null, null, true));

        ResponseEntity<ResponseDTO<AuthMeResponse>> response =
                authController.me(authentication);

        verify(authQueryService).getCurrentUser(1L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getData().getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void updateProfile_returnsUpdatedCurrentUser() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(1L, null, List.of());
        AuthProfileUpdateRequest request =
                new AuthProfileUpdateRequest("tester", "MALE");
        when(authProfileService.updateProfile(1L, request))
                .thenReturn(new AuthMeResponse(1L, "user@test.com", "tester", "MALE", false));

        ResponseEntity<ResponseDTO<AuthMeResponse>> response =
                authController.updateProfile(authentication, request);

        verify(authProfileService).updateProfile(1L, request);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getNickName()).isEqualTo("tester");
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
        assertThat(cookie(response.getCookies(), "access_token").getValue())
                .isEqualTo("new-access");
        assertThat(cookie(response.getCookies(), "refresh_token").getValue())
                .isEqualTo("new-refresh");
        assertThat(response.getHeader("Authorization")).isNull();
    }

    @Test
    void logout_usesRefreshCookieAndClearsAllAuthCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("refresh_token", "refresh-token"),
                new Cookie("session_id", "session-1")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<Void> result =
                authController.logout(request, response);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        verify(authLogoutService).logout("refresh-token", "session-1");
        assertThat(cookie(response.getCookies(), "access_token").getMaxAge())
                .isZero();
        assertThat(cookie(response.getCookies(), "refresh_token").getMaxAge())
                .isZero();
        assertThat(cookie(response.getCookies(), "session_id").getMaxAge())
                .isZero();
    }

    @Test
    void logout_clearsAuthCookiesEvenWhenSessionDeleteFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("refresh_token", "refresh-token"),
                new Cookie("session_id", "session-1")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new RuntimeException("Invalid refresh token"))
                .when(authLogoutService).logout("refresh-token", "session-1");

        ResponseEntity<Void> result =
                authController.logout(request, response);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(cookie(response.getCookies(), "access_token").getMaxAge())
                .isZero();
        assertThat(cookie(response.getCookies(), "refresh_token").getMaxAge())
                .isZero();
        assertThat(cookie(response.getCookies(), "session_id").getMaxAge())
                .isZero();
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
