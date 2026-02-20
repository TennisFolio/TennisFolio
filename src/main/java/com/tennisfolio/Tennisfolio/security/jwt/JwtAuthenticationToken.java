package com.tennisfolio.Tennisfolio.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;

    public JwtAuthenticationToken(Long userId){
        super(null);
        this.userId = userId;
        setAuthenticated(true);
    }
    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
