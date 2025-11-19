package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApiWorker{
    private final Map<RapidApi, StrategyApiTemplate<?, ?>> strategyApiTemplateMap;
    private final RedisRateLimiter rateLimiter;
    private final ConcurrentHashMap<String, AtomicInteger> tpsMap = new ConcurrentHashMap<>();

    public ApiWorker(List<StrategyApiTemplate<?, ?>> strategies, RedisRateLimiter rateLimiter) {
        this.strategyApiTemplateMap = strategies.stream()
                .collect(Collectors.toMap(StrategyApiTemplate::getEndPoint, s-> s));

        this.rateLimiter = rateLimiter;
    }

    public <T, E> E process(RapidApi endpoint, Object ... params){

        rateLimiter.acquireBlocking();

        // 현재 초 (예: 20250213-11:05:32)
        String nowSec = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss"));

        int count = tpsMap
                .computeIfAbsent(nowSec, k -> new AtomicInteger(0))
                .incrementAndGet();

        log.info("API CALL TPS_CHECK {} → {}th call", nowSec, count);

        log.info("[RedisRateLimiter] allowed API call → {}", endpoint);

        StrategyApiTemplate<T, E> strategy = (StrategyApiTemplate<T, E>) strategyApiTemplateMap.get(endpoint);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + endpoint);
        }

        return strategy.execute(params);
    }
}
