package com.tennisfolio.Tennisfolio.security.oauth.handler;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginSuccessHandlerTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RefreshTokenService refreshTokenService;

    OAuthLoginSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        successHandler = new OAuthLoginSuccessHandler(
                jwtTokenProvider,
                refreshTokenService,
                "http://localhost:4173"
        );
    }

    @Test
    void oauthSuccess_setsAuthCookiesAndRedirects() throws Exception {
        Long userId = 1L;

        CustomOAuth2User principal = CustomOAuth2User.builder()
                .userId(userId)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of()
                );

        when(jwtTokenProvider.createAccessToken(userId))
                .thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(userId))
                .thenReturn("refresh-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication
        );

        verify(jwtTokenProvider).createAccessToken(userId);
        verify(jwtTokenProvider).createRefreshToken(userId);
        verify(refreshTokenService)
                .save(eq(userId), anyString(), eq("refresh-token"));

        assertThat(response.getHeader("Authorization")).isNull();

        Cookie[] cookies = response.getCookies();
        assertThat(cookies).hasSize(3);
        assertThat(cookie(cookies, "access_token").getValue())
                .isEqualTo("access-token");
        assertThat(cookie(cookies, "refresh_token").getValue())
                .isEqualTo("refresh-token");
        assertThat(cookie(cookies, "session_id").getValue())
                .isNotBlank();

        assertThat(cookie(cookies, "access_token").isHttpOnly()).isTrue();
        assertThat(cookie(cookies, "access_token").getSecure()).isTrue();
        assertThat(cookie(cookies, "access_token").getPath()).isEqualTo("/");
        assertThat(cookie(cookies, "access_token").getAttribute("SameSite"))
                .isEqualTo("Lax");
        assertThat(cookie(cookies, "refresh_token").getMaxAge())
                .isEqualTo(60 * 60 * 24 * 14);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:4173");
    }

    private static Cookie cookie(Cookie[] cookies, String name) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
