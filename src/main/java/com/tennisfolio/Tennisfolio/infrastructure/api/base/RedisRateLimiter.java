package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final int limitPerSecond = 5;
    private final long windowMillis = 1000;

    public RedisRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquire(){
        String key = "rate:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Long count = redisTemplate.opsForValue().increment(key);

        if(count == 1){
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }

        return count <= limitPerSecond;
    }

    public void acquireBlocking(){
        while (true) {
            long now = System.currentTimeMillis();
            String key = "rate-win";

            // 1) 윈도우 시작 시각 계산 (지금 기준 1000ms 전)
            long windowStart = now - windowMillis;

            // 2) 1초(1000ms) 이전의 요청 기록 삭제
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 3) 최근 1초 안에 몇 번 호출했는지 카운트
            Long count = redisTemplate.opsForZSet().zCard(key);

            // 4) 아직 limit 미만이면 → 이번 호출을 허용하고 나감
            if (count != null && count < limitPerSecond) {
                redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
                redisTemplate.expire(key, Duration.ofSeconds(2));
                return;
            }

            // 5) limit을 넘었으면 잠깐 쉬었다가 다시 시도
            try { Thread.sleep(50); } catch (InterruptedException ignore) {}
        }
    }
}
