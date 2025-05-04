package com.tennisfolio.Tennisfolio.exception;


import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class RapidApiException extends RuntimeException{

    private final ExceptionCode exceptionCode;
    public RapidApiException(ExceptionCode code) {
        super(code.getMessage());

        this.exceptionCode = code;
    }

}
