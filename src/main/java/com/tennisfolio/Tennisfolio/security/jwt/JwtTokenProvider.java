package com.tennisfolio.Tennisfolio.security.jwt;

import com.tennisfolio.Tennisfolio.exception.JwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE ="typ";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-exp-seconds:1800}")
    private long accessExpSeconds;

    @Value("${jwt.refresh-exp-seconds:1209600}")
    private long refreshExpSeconds;

    private Key key;

    @PostConstruct
    void init(){

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, "access", accessExpSeconds);
    }

    /** Refresh Token 생성 (role 넣어도 되지만 보통 최소정보 권장) */
    public String createRefreshToken(Long userId) {
        return createToken(userId, "refresh", refreshExpSeconds);
    }

    private String createToken(Long userId, String type, long expSeconds){
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(CLAIM_TYPE, type))
                .signWith(key, SignatureAlgorithm.HS256);

        return builder.compact();

    }

    public void validateOrThrow(String token){
        try{
            parser().parseClaimsJws(token);
        } catch (ExpiredJwtException e){
            throw new JwtTokenException(e.getMessage());
        } catch (JwtException | IllegalArgumentException e){
            throw new JwtTokenException(e.getMessage());
        }
    }

    public JwtClaims parseClaims(String token){
        Claims claims = parser().parseClaimsJws(token).getBody();

        Long userId = Long.valueOf(claims.getSubject());
        String type = claims.get(CLAIM_TYPE, String.class);

        Instant iat = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
        Instant exp = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;

        return new JwtClaims(userId, type, iat, exp);
    }

    public Long getUserId(String token) {
        JwtClaims claims = parseClaims(token);
        return claims.getUserId();
    }

    public Authentication getAuthentication(String accessToken){
        // 1️⃣ Claims 파싱 (서명/만료 검증은 Filter에서 이미 했다고 가정)
        JwtClaims claims = parseClaims(accessToken);

        // 2️⃣ Subject에서 userId 추출
        Long userId = Long.valueOf(claims.getUserId());

        // 3️⃣ Authentication 객체 생성
        return new JwtAuthenticationToken(userId);
    }

    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
    }
}
