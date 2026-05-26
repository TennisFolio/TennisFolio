package com.tennisfolio.Tennisfolio.matching.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class CompetitionAdminTokenService {
    private static final String CLAIM_PURPOSE = "purpose";
    private static final String CLAIM_PURPOSE_VALUE = "COMPETITION_ADMIN";
    private static final long EXP_SECONDS = 60L * 60L * 24L * 30L;

    private final String secret;
    private Key key;

    public CompetitionAdminTokenService(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String publicId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(EXP_SECONDS);

        return Jwts.builder()
                .setSubject(publicId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .addClaims(Map.of(CLAIM_PURPOSE, CLAIM_PURPOSE_VALUE))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndGetPublicId(String token) {
        if (token == null || token.isBlank()) {
            throw forbidden();
        }

        try {
            Claims claims = parser().parseClaimsJws(token).getBody();
            String purpose = claims.get(CLAIM_PURPOSE, String.class);
            if (!CLAIM_PURPOSE_VALUE.equals(purpose)) {
                throw forbidden();
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw forbidden();
        }
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
    }

    private ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 올바르지 않습니다. 다시 로그인해 주세요.");
    }
}
