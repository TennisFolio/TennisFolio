package com.tennisfolio.Tennisfolio.mock;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FakeRedisTemplate extends RedisTemplate<String, Object> {
    private final Map<String, Object> store = new HashMap<>();
    private final Map<String, Long> expiry = new HashMap<>();

    @Override
    public Set<String> keys(String pattern){
        String regex = pattern.replace("*", ".*");
        return store.keySet().stream()
                .filter(k -> k.matches(regex))
                .collect(Collectors.toSet());
    }

    @Override
    public ValueOperations<String, Object> opsForValue(){
        return new ValueOperations<String ,Object>(){
            @Override
            public void set(String key, Object value) {
                store.put(key, value);
            }

            @Override
            public void set(String key, Object value, long offset) {
                store.put(key, value);
            }


            @Override
            public void set(String key, Object value, long timeout, TimeUnit unit) {
                store.put(key, value);
                expiry.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
            }


            @Override
            public Object get(Object key) {
                return store.get(key.toString());
            }

            @Override
            public List<Object> multiGet(Collection<String> keys) {
                List<Object> resultList = new ArrayList<>();
                keys.stream().forEach(p -> {
                    resultList.add(store.get(p));
                });
                return resultList;
            }





            @Override
            public Boolean setIfAbsent(String key, Object value) {
                return null;
            }

            @Override
            public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
                return null;
            }

            @Override
            public Boolean setIfPresent(String key, Object value) {
                return null;
            }

            @Override
            public Boolean setIfPresent(String key, Object value, long timeout, TimeUnit unit) {
                return null;
            }

            @Override
            public void multiSet(Map<? extends String, ?> map) {

            }

            @Override
            public Boolean multiSetIfAbsent(Map<? extends String, ?> map) {
                return null;
            }

            @Override
            public Object getAndDelete(String key) {
                return null;
            }

            @Override
            public Object getAndExpire(String key, long timeout, TimeUnit unit) {
                return null;
            }

            @Override
            public Object getAndExpire(String key, Duration timeout) {
                return null;
            }

            @Override
            public Object getAndPersist(String key) {
                return null;
            }

            @Override
            public Object getAndSet(String key, Object value) {
                return null;
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
            public RedisOperations<String, Object> getOperations() {
                return null;
            }

        };
    }
    public Object getValue(String key){
        return store.get(key);
    }


}
