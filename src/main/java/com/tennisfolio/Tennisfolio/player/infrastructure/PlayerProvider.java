package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayerProvider {
    private final PlayerRepository playerRepository;
    private final PlayerImageService playerImageService;
    private final ApiWorker apiWorker;

    public PlayerProvider(PlayerRepository playerRepository, PlayerImageService playerImageService, ApiWorker apiWorker) {
        this.playerRepository = playerRepository;
        this.playerImageService = playerImageService;
        this.apiWorker = apiWorker;
    }

    public Player provide(String rapidId){
        return playerRepository.findByRapidPlayerId(rapidId)
                .orElseGet(() -> {
                    try{
                        PlayerAggregate agg =apiWorker.process(RapidApi.TEAMDETAILS, rapidId);
                        Player player = agg.toPlayer();
                        String path = playerImageService.fetchImage(rapidId);
                        player.updateProfileImage(path);

                        return playerRepository.save(player);
                    }catch(DataIntegrityViolationException e){
                        return playerRepository.findByRapidPlayerId(rapidId).orElseThrow(() -> e);
                    }

                });
    }
}
