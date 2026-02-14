package com.tennisfolio.Tennisfolio.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class JwtClaims {
    private Long userId;
    private String type;
    private Instant issuedAt;
    private Instant expiresAt;
}
