package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import lombok.Getter;

@Getter
public class PlayerNotFoundException extends RuntimeException {
  private final ExceptionCode exceptionCode;
    public PlayerNotFoundException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
