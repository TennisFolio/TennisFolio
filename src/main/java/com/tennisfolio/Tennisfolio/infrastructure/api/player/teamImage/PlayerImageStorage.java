package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage;

import com.tennisfolio.Tennisfolio.common.ImageDirectory;
import com.tennisfolio.Tennisfolio.common.image.S3Uploader;
import org.springframework.stereotype.Component;

@Component
public class PlayerImageStorage {

    private final S3Uploader s3Uploader;
    private final ImageFormatDetector formatDetector;

    public PlayerImageStorage(S3Uploader s3Uploader, ImageFormatDetector formatDetector) {
        this.s3Uploader = s3Uploader;
        this.formatDetector = formatDetector;
    }

    public String store(String fileName, byte[] data){

        String imageName = ImageDirectory.PLAYER.getDirectory() + fileName;
        String ext = formatDetector.detectExtension(data);
        String key = imageName + ext;
        String contentType = formatDetector.detectMime(data);
        s3Uploader.upload(data, key, contentType);
        return imageName;
    }
}
