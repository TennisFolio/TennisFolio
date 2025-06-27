package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage;

import org.springframework.stereotype.Service;

@Service
public class PlayerImageService {
    private final TeamImageDownloader downloader;
    private final PlayerImageStorage storage;

    public PlayerImageService(TeamImageDownloader downloader, PlayerImageStorage storage) {
        this.downloader = downloader;
        this.storage = storage;
    }

    public String fetchImage(String rapidId){
        byte[] data = downloader.download(rapidId);
        String path = storage.store(rapidId, data);
        return path;
    }
}
