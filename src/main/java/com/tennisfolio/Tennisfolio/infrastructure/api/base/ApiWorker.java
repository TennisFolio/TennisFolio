package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.google.common.util.concurrent.RateLimiter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ApiWorker<T,E>{
    private final Map<RapidApi, StrategyApiTemplate<T, E>> strategyApiTemplateMap;
    private final RateLimiter rateLimiter;

    public ApiWorker(List<StrategyApiTemplate<T, E>> strategies) {
        this.strategyApiTemplateMap = strategies.stream()
                .collect(Collectors.toMap(StrategyApiTemplate::getEndPoint, s-> s));
        this.rateLimiter = RateLimiter.create(6.0);
    }

    public E process(RapidApi endpoint, Object ... params){
        rateLimiter.acquire();

        StrategyApiTemplate<T, E> strategy = strategyApiTemplateMap.get(endpoint);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + endpoint);
        }

        return strategy.execute(params);
    }
}
