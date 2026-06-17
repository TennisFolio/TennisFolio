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
