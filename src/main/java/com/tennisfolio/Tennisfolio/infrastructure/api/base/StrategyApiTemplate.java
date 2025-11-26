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
    private final ApiCallCounter apiCallCounter;
    private final RapidApi endpoint;

    // 생성자
    public StrategyApiTemplate(
            ApiCaller apiCaller,
            ResponseParser<T> parser,
            EntityMapper<T, E> mapper,
            ApiCallCounter apiCallCounter,
            RapidApi endpoint) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.mapper = mapper;
        this.apiCallCounter = apiCallCounter;
        this.endpoint = endpoint;
    }

    // api 조회 후 엔티티 변환
    public E execute(Object... params){
        // api 호출
        String responseStr = apiCaller.callApi(endpoint, params);
        if(responseStr == null) return null;
        // api 호출 증가
        apiCallCounter.increment(endpoint.getMethodName());
        // response to DTO
        T dto = parser.parse(responseStr);
        // DTO to Entity
        return mapper.map(dto, params);

    }

    public RapidApi getEndPoint(){
        return endpoint;
    }


}
