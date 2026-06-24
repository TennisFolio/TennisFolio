package com.tennisfolio.Tennisfolio.security.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReissuedToken {
    private String accessToken;
    private String refreshToken;
}
