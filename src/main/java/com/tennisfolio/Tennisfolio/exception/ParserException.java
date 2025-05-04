package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class ParserException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    public ParserException(ExceptionCode code) {
        super(code.getMessage());
        this.exceptionCode = code;
    }
}
