package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.tennisfolio.Tennisfolio.exception.Rapid429Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Slf4j
public class RetryExecutor {

    private static final int MAX_RETRY = 5;
    private static final long BASE_DELAY = 200;
    private static final long MAX_DELAY = 3000;

    public <T> T callWithRetry(Supplier<T> action){
        for(int retry = 0; retry <= MAX_RETRY; retry++){
            try{
                return action.get();
            }
            catch(Rapid429Exception e){
                if(retry == MAX_RETRY){
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
