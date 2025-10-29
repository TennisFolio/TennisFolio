package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class}, basePackages = {"com.tennisfolio.Tennisfoli.web.controller"})
@Hidden
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

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ResponseDTO<Void>> handleIllegalArgumentException(InvalidRequestException e){
        ExceptionCode illegalArgumentException = ExceptionCode.INVALID_REQUEST;
        return ResponseEntity.status(illegalArgumentException.getHttpStatus())
                .body(ResponseDTO.error(e.getExceptionCode().getCode(), e.getExceptionCode().getMessage()));
    }
    @ExceptionHandler(LiveMatchNotFoundException.class)
    public ResponseEntity<ResponseDTO<Void>> handleLiveMatchNotFoundException(LiveMatchNotFoundException e){
        ExceptionCode liveMatchNotFoundException = ExceptionCode.NOT_FOUND;
        return ResponseEntity.status(liveMatchNotFoundException.getHttpStatus())
                .body(ResponseDTO.error(e.getExceptionCode().getCode(), e.getExceptionCode().getMessage()));
    }

    @ExceptionHandler(TestNotFoundException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTestNotFoundException(TestNotFoundException e){
        ExceptionCode testNotFoundException = ExceptionCode.NOT_FOUND;
        return ResponseEntity.status(testNotFoundException.getHttpStatus())
                .body(ResponseDTO.error(e.getExceptionCode().getCode(), e.getExceptionCode().getMessage()));
    }

    // RequestParam, PathVariable 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDTO<?>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("잘못된 요청입니다.");

        return ResponseEntity
                .badRequest() // HTTP 400
                .body(ResponseDTO.error("VALIDATION_ERROR", message));
    }
}
