package com.tennisfolio.Tennisfolio.user.service;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthLogoutServiceTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthLogoutService authLogoutService;

    @Test
    void logout_deletesRefreshTokenByRefreshTokenSubjectAndSessionId() {
        when(jwtTokenProvider.getUserId("refresh-token")).thenReturn(1L);

        authLogoutService.logout("refresh-token", "session-1");

        verify(jwtTokenProvider).validateOrThrow("refresh-token");
        verify(jwtTokenProvider).getUserId("refresh-token");
        verify(refreshTokenService).delete(1L, "session-1");
    }
}
