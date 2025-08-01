package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.RapidApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public abstract class StrategyApiTemplate<T, E> {


    private DecompressorUtil decompressorUtil;
    private final ApiCaller apiCaller;
    private final ResponseParser<T> parser;
    private final EntityMapper<T, E> mapper;
    private final RapidApi endpoint;

    // 생성자
    public StrategyApiTemplate(
                               ApiCaller apiCaller,
                               ResponseParser<T> parser,
                               EntityMapper<T, E> mapper,
                               RapidApi endpoint) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.mapper = mapper;
        this.endpoint = endpoint;
    }

    // api 조회 후 DB 저장
    public E execute(Object... params){
        // api 호출
        String responseStr = apiCaller.callApi(endpoint, params);
        // response to DTO
        T dto = parser.parse(responseStr);
        // DTO to Entity
        return mapper.map(dto, params);

    }

    // api 조회 후 화면에 응답
    public T executeWithoutSave(String params){
        // api호출
        String responseStr = apiCaller.callApi(endpoint, params);
        // response to DTO
        return parser.parse(responseStr);
    }

}
