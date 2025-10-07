package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class ImageFormatDetector {
    private final Tika tika = new Tika();

    public String detectMime(byte[] data){
        return tika.detect(data);
    }

    public String detectExtension(byte[] data){
        return switch (detectMime(data)){
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
