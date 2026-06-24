package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class AuthLogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthLogoutService(
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public void logout(String refreshToken, String sessionId) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        jwtTokenProvider.validateOrThrow(refreshToken);
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        refreshTokenService.delete(userId, sessionId);
    }
}
