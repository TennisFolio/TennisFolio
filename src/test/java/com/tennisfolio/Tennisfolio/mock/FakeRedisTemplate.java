package com.tennisfolio.Tennisfolio.mock;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FakeRedisTemplate extends StringRedisTemplate {
    private final Map<String, String> store = new HashMap<>();
    private final Map<String, Long> expiry = new HashMap<>();

    @Override
    public Set<String> keys(String pattern){
        String regex = pattern.replace("*", ".*");
        return store.keySet().stream()
                .filter(k -> k.matches(regex))
                .collect(Collectors.toSet());
    }

    @Override
    public ValueOperations<String, String> opsForValue(){

        return new ValueOperations<String ,String>(){

            @Override
            public void set(String key, String value) {
                store.put(key, value);
            }

            @Override
            public void set(String key, String value, long offset) {
                store.put(key, value);
            }

            @Override
            public void set(String key, String value, long timeout, TimeUnit unit) {
                store.put(key, value);
                expiry.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
            }

            @Override
            public String get(Object key) {
                return store.get(key.toString());
            }

            @Override
            public List<String> multiGet(Collection<String> keys) {
                List<String> resultList = new ArrayList<>();
                keys.stream().forEach(p -> {
                    resultList.add(store.get(p));
                });
                return resultList;
            }

            @Override
            public Boolean setIfAbsent(String key, String value) {
                return null;
            }

            @Override
            public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
                return null;
            }

            @Override
            public Boolean setIfPresent(String key, String value) {
                return null;
            }

            @Override
            public Boolean setIfPresent(String key, String value, long timeout, TimeUnit unit) {
                return null;
            }

            @Override
            public void multiSet(Map<? extends String, ? extends String> map) {

            }

            @Override
            public Boolean multiSetIfAbsent(Map<? extends String, ? extends String> map) {
                return null;
            }


            @Override
            public String getAndDelete(String key) {
                return "";
            }

            @Override
            public String getAndExpire(String key, long timeout, TimeUnit unit) {
                return "";
            }

            @Override
            public String getAndExpire(String key, Duration timeout) {
                return "";
            }

            @Override
            public String getAndPersist(String key) {
                return "";
            }

            @Override
            public String getAndSet(String key, String value) {
                return "";
            }


            @Override
            public Long increment(String key) {
                return 0L;
            }

            @Override
            public Long increment(String key, long delta) {
                return 0L;
            }

            @Override
            public Double increment(String key, double delta) {
                return 0.0;
            }

            @Override
            public Long decrement(String key) {
                return 0L;
            }

            @Override
            public Long decrement(String key, long delta) {
                return 0L;
            }

            @Override
            public Integer append(String key, String value) {
                return 0;
            }

            @Override
            public String get(String key, long start, long end) {
                return "";
            }



            @Override
            public Long size(String key) {
                return 0L;
            }

            @Override
            public Boolean setBit(String key, long offset, boolean value) {
                return null;
            }

            @Override
            public Boolean getBit(String key, long offset) {
                return null;
            }

            @Override
            public List<Long> bitField(String key, BitFieldSubCommands subCommands) {
                return List.of();
            }

            @Override
            public RedisOperations<String, String> getOperations() {
                return null;
            }
        };
    }
    public Object getValue(String key){
        return store.get(key);
    }


}
