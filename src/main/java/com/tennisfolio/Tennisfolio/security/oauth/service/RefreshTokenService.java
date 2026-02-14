package com.tennisfolio.Tennisfolio.security.oauth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${jwt.refresh-token-expiration-seconds}")
    private long refreshExpSeconds;

    public void save(Long userId, String sessionId, String refreshToken){
        String key = key(userId, sessionId);
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(refreshExpSeconds));
    }

    public String get(Long userId, String sessionId){
        String key = key(userId, sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(Long userId, String sessionId){
        String key = key(userId, sessionId);
        redisTemplate.delete(key);
    }

    private String key(Long userId, String sessionId) {
        return KEY_PREFIX + ":" + userId + ":" + sessionId;
    }
}
