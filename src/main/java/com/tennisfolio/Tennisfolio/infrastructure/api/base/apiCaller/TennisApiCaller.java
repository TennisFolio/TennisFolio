package com.tennisfolio.Tennisfolio.infrastructure.api.base.apiCaller;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.config.HttpClientConfig;
import com.tennisfolio.Tennisfolio.exception.RapidApiException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Component
public class TennisApiCaller implements ApiCaller {

    // API Key
    @Value("${x-rapidapi-key}")
    protected String rapidApiKey;
    // API Url
    protected String url = "https://tennisapi1.p.rapidapi.com/api/";

    private final HttpClientConfig httpClientConfig;

    private DecompressorUtil decompressorUtil;

    @Autowired
    public TennisApiCaller(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @Override
    public String callApi(RapidApi endpoint, Object... params) {
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
            HttpResponse<byte[]> response = httpClientConfig.httpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() != 200){
                for(Object param : params){
                    System.out.println(param);
                }
                throw new Exception();
            }

            return decodeResponse(response);
        }catch(Exception e){
            e.printStackTrace();
            throw new RapidApiException(ExceptionCode.RAPID_ERROR);
        }
    }

    private String decodeResponse(HttpResponse<byte[]> response){

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
