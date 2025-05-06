package com.tennisfolio.Tennisfolio.api.base;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.RapidApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public abstract class AbstractApiTemplate<T, E> {
    // API Key
    @Value("${x-rapidapi-key}")
    protected String rapidApiKey;

    // API Url
    protected String url = "https://tennisapi1.p.rapidapi.com/api/";

    // gzip, deflate 방식 처리
    protected final DecompressorUtil decompressorUtil;


    // 생성자
    public AbstractApiTemplate(DecompressorUtil decompressorUtil){
        this.decompressorUtil = decompressorUtil;
    }

    // api 조회 후 DB 저장
    public E execute(String params){
        // api 호출
        HttpResponse<byte[]> response = callApi(params);

        if(response.statusCode() != 200){
            return null;
        }
        // response to Str
        String responseStr = decodeResponse(response);
        // response to DTO
        T responseDto = toDTO(responseStr);
        // DTO to Entity
        E entity = toEntity(responseDto);
        // Entity DB에 저장
        return saveEntity(entity);

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
        T responseDto = toDTO(responseStr);

        return responseDto;
    }

    // response To dto
    public abstract T toDTO(String response);

    // dto To Entity
    public abstract E toEntity(T dto);

    // url을 가져옵니다.
    public abstract String getEndpointUrl(Object... params);

    public abstract E saveEntity(E entity);

    // api 호출
    public HttpResponse<byte[]> callApi(String params){
        String responseText = "";
        try{
            HttpClient client = HttpClient.newHttpClient();
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(url)
                    .path(getEndpointUrl(params))
                    .build()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", "tennisapi1.p.rapidapi.com")
                    .header("Accept-Encoding", "gzip, deflate")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            return response;
        }catch(Exception e){
            throw new RapidApiException(ExceptionCode.RAPID_ERROR);
        }

    }

    public String decodeResponse(HttpResponse<byte[]> response){
        String responseText = "";
        byte[] responseBody = response.body();
        try{
            //Content-Encoding 확인
            Optional<String> contentEncoding = response.headers().firstValue("Content-Encoding");
            System.out.println("Content-Encoding: " + contentEncoding.orElse("None"));
            if(contentEncoding.isPresent() && "gzip".equalsIgnoreCase(contentEncoding.get())){
                // GZIP 압축 해제
                responseText = decompressorUtil.decompressGzip(responseBody);
            }else if (contentEncoding.isPresent() && "deflate".equalsIgnoreCase(contentEncoding.get())){
                responseText = decompressorUtil.decompressDeflate(responseBody);
            }
            else{
                responseText = new String(responseBody, "UTF-8");
            }

            return responseText;
        }catch(Exception e){
            throw new RapidApiException(ExceptionCode.RAPID_ERROR);
        }

    }
}
