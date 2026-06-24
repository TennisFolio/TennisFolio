package com.tennisfolio.Tennisfolio.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(mock(JwtTokenProvider.class));

    @Test
    void shouldNotFilter_skipsRefreshTokenBasedAuthEndpoints() {
        assertThat(shouldSkip("/api/auth/reissue")).isTrue();
        assertThat(shouldSkip("/api/auth/reIssue")).isTrue();
        assertThat(shouldSkip("/api/auth/logout")).isTrue();
    }

    @Test
    void shouldNotFilter_keepsFilteringAuthenticatedEndpoints() {
        assertThat(shouldSkip("/api/auth/me")).isFalse();
    }

    private boolean shouldSkip(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        return filter.shouldNotFilter(request);
    }
}
