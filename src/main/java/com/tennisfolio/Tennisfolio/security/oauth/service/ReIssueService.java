package com.tennisfolio.Tennisfolio.security.oauth.service;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class ReIssueService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public ReIssueService(JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public String reIssue(
            String refreshToken,
            String sessionId
    ){
        jwtTokenProvider.validateOrThrow(refreshToken);

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        String saved = refreshTokenService.get(userId, sessionId);
        if(saved == null || !saved.equals(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }

        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.save(
                userId,
                sessionId,
                newRefreshToken
        );

        return jwtTokenProvider.createAccessToken(userId);

    }
}
