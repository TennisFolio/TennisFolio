package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.tennisfolio.Tennisfolio.config.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class ApiCallCounterTest {

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private ApiCallCounter apiCallCounter;

    @Test
    void API_호출_시_redis에_데이터_없을_때_레디스에_추가(){
        String key = "test";
        String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
                        .format(DateTimeFormatter.BASIC_ISO_DATE);

        apiCallCounter.increment(key);

        String result = redis.opsForValue().get("api:"+key+":"+today);

        assertThat(result).isEqualTo("1");
    }

    @Test
    void API_호출_시_redis에_데이터_있을_때_플러스_1(){

        String key = "test1";
        String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String testKey = "api:"+key+":"+today;

        redis.opsForValue().increment(testKey);

        apiCallCounter.increment(key);

        String result = redis.opsForValue().get(testKey);

        assertThat(result).isEqualTo("2");

    }

}
