package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ApiCallCounter {

    private final StringRedisTemplate stringRedisTemplate;

    public ApiCallCounter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void increment(String apiName){
        String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String key = "api:" + apiName + ":" + today;

        stringRedisTemplate.opsForValue().increment(key);
    }
}
