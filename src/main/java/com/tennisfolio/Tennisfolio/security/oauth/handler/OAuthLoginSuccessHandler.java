package com.tennisfolio.Tennisfolio.security.oauth.handler;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.dto.LoginToken;
import com.tennisfolio.Tennisfolio.security.oauth.repository.OAuthAccountRepository;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Log4j2
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final String frontendUrl;


    public OAuthLoginSuccessHandler(JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService, @Value("${app.frontend.url}") String frontendUrl) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 로그인 정보 조회
        CustomOAuth2User oAuth2User = extractOAuthUser(authentication);

        Long userId = extractUserId(oAuth2User);

        // 토큰 발급
        LoginToken token = issueToken(userId);

        log.info("OAuth Login Success: accessToken={}, refreshToken={}, sessionId={}",
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getSessionId()
        );

        // refresh token 저장
        persistRefreshToken(userId, token);

        // response 세팅
        applyTokenToResponse(response, token);

        // response 리다이렉트
        response.sendRedirect(frontendUrl);
    }

    private void applyTokenToResponse(HttpServletResponse response, LoginToken token) {
        response.setHeader("Authorization", "Bearer " + token.getAccessToken());
        response.addCookie(createRefreshTokenCookie(token.getRefreshToken()));
        response.addCookie(sessionIdCookie(token.getSessionId()));

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Long extractUserId(CustomOAuth2User oAuth2User) {
        return oAuth2User.getUserId();
    }

    private static CustomOAuth2User extractOAuthUser(Authentication authentication) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        return oAuth2User;
    }

    private LoginToken issueToken(Long userId) {
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        String sessionId = UUID.randomUUID().toString();

        return new LoginToken(accessToken, refreshToken, sessionId);
    }

    private void persistRefreshToken(Long userId, LoginToken token) {
        refreshTokenService.save(userId, token.getSessionId(), token.getRefreshToken());
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        return cookie;
    }

    private Cookie sessionIdCookie(String sessionId) {
        Cookie cookie = new Cookie("session_id", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);      // HTTPS 필수
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); // refreshToken과 동일
        return cookie;
    }
}
