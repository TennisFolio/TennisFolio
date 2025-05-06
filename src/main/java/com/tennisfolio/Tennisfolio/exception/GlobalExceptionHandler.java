package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RapidApiException.class)
    public ResponseEntity<ResponseDTO<Void>> handleRapidApiException(RapidApiException e){
        ExceptionCode rapidException = ExceptionCode.RAPID_ERROR;
        return ResponseEntity.status(rapidException.getHttpStatus())
                .body(ResponseDTO.error(e.getExceptionCode().getCode(), e.getExceptionCode().getMessage()));
    }

    @ExceptionHandler(ParserException.class)
    public ResponseEntity<ResponseDTO<Void>> handleParserException(ParserException e){
        ExceptionCode parserException = ExceptionCode.PARSER_ERROR;
        return ResponseEntity.status(parserException.getHttpStatus())
                .body(ResponseDTO.error(e.getExceptionCode().getCode(), e.getExceptionCode().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleException(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error(e.getMessage(), "ERROR"));
    }
}
