package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
  private final ExceptionCode exceptionCode;
    public NotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
