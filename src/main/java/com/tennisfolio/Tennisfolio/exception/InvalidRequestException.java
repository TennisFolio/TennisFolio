package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {
  private final ExceptionCode exceptionCode;
    public InvalidRequestException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
