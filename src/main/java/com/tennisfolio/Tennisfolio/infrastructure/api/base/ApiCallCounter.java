package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ApiCallCounter {

    private final StringRedisTemplate stringRedisTemplate;

    public ApiCallCounter( @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void increment(String apiName){
        try{
            if(stringRedisTemplate == null){
                log.warn("RedisTemplate이 활성화되어 있지 않습니다.");
                return;
            }
            String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.BASIC_ISO_DATE);

            String key = "api:" + apiName + ":" + today;

            stringRedisTemplate.opsForValue().increment(key);
        }catch(RedisConnectionFailureException e){
            log.error("Redis 연결 실패! fallback 모드로 동작합니다.", e);
        }

    }
}
