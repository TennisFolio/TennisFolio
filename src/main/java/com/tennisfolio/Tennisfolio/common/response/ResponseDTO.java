package com.tennisfolio.Tennisfolio.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ResponseDTO<T> {
    private final String code;
    private final String message;
    private final T data;

    public ResponseDTO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseDTO<T> success(T data){
        return new ResponseDTO<>("0000", "성공", data);
    }

    public static <T> ResponseDTO<T> success(){
        return new ResponseDTO<>("0000", "성공", null);
    }

    public static <T> ResponseDTO<T> error(String code, String message){
        return new ResponseDTO<>(code, message, null);
    }
}
