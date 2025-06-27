package com.tennisfolio.Tennisfolio.player.application;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    private final StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate;
    private final PlayerImageService playerImageService;
    private final PlayerRepository playerRepository;
    public PlayerService(@Qualifier("teamDetailsTemplate") StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate, PlayerImageService playerImageService
            , PlayerRepository playerRepository){
        this.teamDetailsTemplate = teamDetailsTemplate;
        this.playerImageService = playerImageService;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public Player getOrCreatePlayerByRapidId(String rapidId) {
        return playerRepository.findByRapidPlayerId(rapidId)
                .orElseGet(() -> {
                    Player player = teamDetailsTemplate.execute(rapidId).toPlayer();
                    String path = playerImageService.fetchImage(rapidId);
                    player.updateProfileImage(path);
                    return playerRepository.save(player);
                });
    }


}
