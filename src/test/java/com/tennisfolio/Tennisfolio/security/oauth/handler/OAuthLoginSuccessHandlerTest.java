package com.tennisfolio.Tennisfolio.security.oauth.handler;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    OAuthLoginSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        // 프론트엔드 URL 설정 (테스트용)
        successHandler = new OAuthLoginSuccessHandler(jwtTokenProvider, refreshTokenService, "http://localhost:4173");
    }

    @Test
    void 로그인_성공시_토큰발급_저장_응답세팅() throws Exception {

        // given
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

        // when
        successHandler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication
        );

        // then

        // 1️⃣ 토큰 생성 호출 검증
        verify(jwtTokenProvider).createAccessToken(userId);
        verify(jwtTokenProvider).createRefreshToken(userId);

        // 2️⃣ refresh token 저장 검증
        verify(refreshTokenService)
                .save(eq(userId), anyString(), eq("refresh-token"));

        // 3️⃣ 헤더 검증
        assertThat(response.getHeader("Authorization"))
                .isEqualTo("Bearer access-token");

        // 4️⃣ 쿠키 검증
        Cookie[] cookies = response.getCookies();
        assertThat(cookies).hasSize(2);

        assertThat(Arrays.stream(cookies)
                .anyMatch(c -> c.getName().equals("refresh_token")))
                .isTrue();

        assertThat(Arrays.stream(cookies)
                .anyMatch(c -> c.getName().equals("session_id")))
                .isTrue();

        // 5️⃣ 리다이렉트 검증
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:4173");
    }
}
