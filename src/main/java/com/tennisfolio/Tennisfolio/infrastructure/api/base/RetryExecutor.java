package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.tennisfolio.Tennisfolio.exception.Rapid429Exception;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Slf4j
public class RetryExecutor {

    private static final int MAX_RETRY = 5;
    private static final long BASE_DELAY = 200;
    private static final long MAX_DELAY = 3000;

    private final Counter retryAttempts;
    private final Counter retryExhausted;

    public RetryExecutor(MeterRegistry registry) {
        this.retryAttempts   = registry.counter("external_api_retry_attempts_total");
        this.retryExhausted  = registry.counter("external_api_retry_exhausted_total");
    }

    public <T> T callWithRetry(Supplier<T> action){
        for(int retry = 0; retry <= MAX_RETRY; retry++){
            try{
                return action.get();
            }
            catch(Rapid429Exception e){

                retryAttempts.increment();
                if(retry == MAX_RETRY){
                    retryExhausted.increment();
                    throw e;
                }
            }

            long sleep = Math.min(BASE_DELAY * (1L << retry), MAX_DELAY);
            log.info("[429 BACKOFF] retry=" + retry + ", sleep=" + sleep + "ms");

            try{
                Thread.sleep(sleep);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", ie);
            }

        }
        throw new RuntimeException("Unexpected retry exit");
    }
}
