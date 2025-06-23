package com.tennisfolio.Tennisfolio.api.teamImage;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.ImageDirectory;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.common.image.S3Uploader;
import com.tennisfolio.Tennisfolio.exception.ParserException;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class TeamImage {
    // API Key
    @Value("${x-rapidapi-key}")
    private String rapidApiKey;

    // 이미지 저장 경로
    @Value("${image.upload-dir}")
    private String uploadDirectory;

    private String url = "https://tennisapi1.p.rapidapi.com/api/";

    private final PlayerRepository playerRepository;

    private final S3Uploader s3Uploader;

    public TeamImage(PlayerRepository playerRepository, S3Uploader s3Uploader){
        this.playerRepository = playerRepository;
        this.s3Uploader = s3Uploader;
    }

    // 이미지 저장 프로세스
    public String saveImage(String params){
        byte[] imageByte = callImageApi(params);

        String path = storeImage(imageByte,params);

        savePlayerImage(params, path);

        return path;
    }
    
    // 이미지 데이터 호출  
    private byte[] callImageApi(String params){
        byte[] imageData = null;
        try {
            // HttpClient 생성
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(url)
                    .path(RapidApi.TEAMIMAGE.getParam(params))
                    .build()
                    .toUri();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", "tennisapi1.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            InputStream inputStream = response.body();
            imageData = inputStream.readAllBytes();

        }catch(Exception e){
            throw new ParserException(ExceptionCode.PARSER_ERROR);
        }

        return imageData;
    }

    // 이미지 저장
    private String storeImage(byte[] imageData, String fileName) {

        String imageName = ImageDirectory.PLAYER.getDirectory() + fileName;
        String keyName = imageName + ".jpg";

        String contentType = "image/jpeg";

        s3Uploader.upload(imageData, keyName, contentType);

        return imageName;
    }

    // 이미지 Path DB 저장
    private void savePlayerImage(String params, String path){
        playerRepository.findByRapidPlayerId(params).ifPresent(player -> {
           player.setImage(path);
           playerRepository.save(player);
        });

    }
}
