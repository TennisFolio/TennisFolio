package com.tennisfolio.Tennisfolio.security.oauth.handler;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.dto.CustomOAuth2User;
import com.tennisfolio.Tennisfolio.security.oauth.dto.LoginToken;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static com.tennisfolio.Tennisfolio.util.CookieUtils.createHttpOnlyCookie;

@Component
@Log4j2
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 60 * 30;
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 60 * 60 * 24 * 14;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final String frontendUrl;

    public OAuthLoginSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = extractOAuthUser(authentication);
        Long userId = extractUserId(oAuth2User);
        LoginToken token = issueToken(userId);

        log.info("OAuth Login Success: userId={}, sessionId={}",
                userId,
                token.getSessionId()
        );

        persistRefreshToken(userId, token);
        applyTokenToResponse(response, token);
        response.sendRedirect(frontendUrl);
    }

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

    private Long extractUserId(CustomOAuth2User oAuth2User) {
        return oAuth2User.getUserId();
    }

    private static CustomOAuth2User extractOAuthUser(Authentication authentication) {
        return (CustomOAuth2User) authentication.getPrincipal();
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
}
