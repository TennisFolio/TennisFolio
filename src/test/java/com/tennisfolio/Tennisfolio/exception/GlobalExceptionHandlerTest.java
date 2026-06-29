package com.tennisfolio.Tennisfolio.exception;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResponseStatusException_usesReasonAsResponseMessage() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "관리자 비밀번호가 올바르지 않습니다."
        );

        ResponseEntity<ResponseDTO<Void>> response = handler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getCode());
        assertEquals("관리자 비밀번호가 올바르지 않습니다.", response.getBody().getMessage());
    }

    @Test
    void handleException_returnsKoreanFallbackMessage() {
        ResponseEntity<ResponseDTO<Void>> response = handler.handleException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.", response.getBody().getMessage());
    }
}
