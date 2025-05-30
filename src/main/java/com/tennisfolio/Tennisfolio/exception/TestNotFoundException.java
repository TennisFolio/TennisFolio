package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class TestNotFoundException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    public TestNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
