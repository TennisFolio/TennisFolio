package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApiWorker{
    private final Map<RapidApi, StrategyApiTemplate<?, ?>> strategyApiTemplateMap;
    private final RateLimiter rateLimiter;

    public ApiWorker(List<StrategyApiTemplate<?, ?>> strategies) {
        this.strategyApiTemplateMap = strategies.stream()
                .collect(Collectors.toMap(StrategyApiTemplate::getEndPoint, s-> s));
        this.rateLimiter = RateLimiter.create(6.0);
    }

    public <T, E> E process(RapidApi endpoint, Object ... params){
        double waited = rateLimiter.acquire();
        log.info("RateLimiter waited " + waited + "s before executing " + endpoint);

        StrategyApiTemplate<T, E> strategy = (StrategyApiTemplate<T, E>) strategyApiTemplateMap.get(endpoint);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + endpoint);
        }

        return strategy.execute(params);
    }
}
