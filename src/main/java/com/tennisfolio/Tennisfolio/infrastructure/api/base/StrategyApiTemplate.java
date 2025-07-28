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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private DecompressorUtil decompressorUtil;
    private final ResponseParser<T> parser;
    private final EntityMapper<T, E> mapper;
    private final RapidApi endpoint;

    // API Key
    @Value("${x-rapidapi-key}")
    protected String rapidApiKey;

    // API Url
    protected String url = "https://tennisapi1.p.rapidapi.com/api/";


    // 생성자
    public StrategyApiTemplate(
                               ResponseParser<T> parser,
                               EntityMapper<T, E> mapper,
                               RapidApi endpoint) {

        this.parser = parser;
        this.mapper = mapper;
        this.endpoint = endpoint;
    }

    // api 조회 후 DB 저장
    public E execute(Object... params){
        // api 호출
        HttpResponse<byte[]> response = callApi(params);

        if(response.statusCode() != 200){
            return null;
        }
        // response to Str
        String responseStr = decodeResponse(response);
        // response to DTO
        T dto = parser.parse(responseStr);
        // DTO to Entity
        return mapper.map(dto, params);

    }

    // api 조회 후 화면에 응답
    public T executeWithoutSave(String params){
        // api 호출
        HttpResponse<byte[]> response = callApi(params);
        if(response.statusCode() != 200){
            return null;
        }
        // response to Str
        String responseStr = decodeResponse(response);
        // response to DTO
        return parser.parse(responseStr);
    }

    // api 호출
    public HttpResponse<byte[]> callApi(Object... params){
        String responseText = "";
        try{

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(url)
                    .path(endpoint.getParam(params))
                    .build()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", "tennisapi1.p.rapidapi.com")
                    .header("Accept-Encoding", "gzip, deflate")
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        }catch(Exception e){
            throw new RapidApiException(ExceptionCode.RAPID_ERROR);
        }

    }

    public String decodeResponse(HttpResponse<byte[]> response){

        try{

            byte[] body = response.body();
            Optional<String> encoding = response.headers().firstValue("Content-Encoding");

            if(encoding.isPresent()){
                String value = encoding.get();
                if ("gzip".equalsIgnoreCase(value)) return decompressorUtil.decompressGzip(body);
                if ("deflate".equalsIgnoreCase(value)) return decompressorUtil.decompressDeflate(body);
            }

            return new String(body, "UTF-8");
        }catch(Exception e){
            throw new RapidApiException(ExceptionCode.RAPID_ERROR);
        }

    }
}
