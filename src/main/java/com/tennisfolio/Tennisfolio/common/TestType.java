package com.tennisfolio.Tennisfolio.common;

import java.util.Arrays;

public enum TestType {
    RACKET,
    STRING,
    ATPPLAYER;

    public static TestType fromString(String type){
        return Arrays.stream(TestType.values())
                .filter(test -> test.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ExceptionCode.NOT_FOUND.getMessage()));
    }
}
