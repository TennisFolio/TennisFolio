package com.tennisfolio.Tennisfolio.config;

import com.tennisfolio.Tennisfolio.security.jwt.JwtTokenProvider;
import com.tennisfolio.Tennisfolio.security.oauth.handler.OAuthLoginSuccessHandler;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestSecurityConfig {

    public JwtTokenProvider jwtTokenProvider(){
        return Mockito.mock(JwtTokenProvider.class);
    }

    public OAuthLoginSuccessHandler oAuthLoginSuccessHandler(){
        return Mockito.mock(OAuthLoginSuccessHandler.class);
    }
}
