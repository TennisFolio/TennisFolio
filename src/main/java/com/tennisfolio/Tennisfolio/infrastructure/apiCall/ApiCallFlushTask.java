package com.tennisfolio.Tennisfolio.infrastructure.apiCall;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.ApiCallJpaRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
@Slf4j
public class ApiCallFlushTask {
    private final StringRedisTemplate redis;
    private final ApiCallJpaRepository apiCallJpaRepository;

    public ApiCallFlushTask(StringRedisTemplate redis, ApiCallJpaRepository apiCallJpaRepository) {
        this.redis = redis;
        this.apiCallJpaRepository = apiCallJpaRepository;
    }

    public void flushDailyApiCounts(){
        String yesterday = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .minusDays(1)
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        Set<String> keys = redis.keys("api:*:" + yesterday);
        if(keys.isEmpty()){
            throw new NoSuchElementException("No API call data found for " + yesterday);
        }

        for(String key : keys){
            String countStr = redis.opsForValue().get(key);
            if (countStr == null) continue;
            long count = Long.parseLong(countStr);
            String apiName = key.split(":")[1];

            apiCallJpaRepository.save(ApiCallEntity.builder()
                    .apiDate(yesterday)
                    .apiName(apiName)
                    .apiCount(count)
                    .build());
        }

        redis.delete(keys);
        log.info("[{}] API 통계 {}건 저장 완료", yesterday, keys.size());
    }
}
