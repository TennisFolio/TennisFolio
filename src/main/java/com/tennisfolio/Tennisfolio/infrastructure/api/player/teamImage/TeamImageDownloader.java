package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class TeamImageDownloader {

    @Value("${x-rapidapi-key}")
    private String apiKey;

    private static final String BASE_URL = "https://tennisapi1.p.rapidapi.com/api/";

    public byte[] download(String playerId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .path(RapidApi.TEAMIMAGE.getParam(playerId))
                    .build().toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", "tennisapi1.p.rapidapi.com")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return response.body().readAllBytes();

        } catch (Exception e) {
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }
    }
}
