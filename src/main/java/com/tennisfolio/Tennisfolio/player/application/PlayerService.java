package com.tennisfolio.Tennisfolio.player.application;
import com.tennisfolio.Tennisfolio.common.aop.SkipLog;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.infrastructure.api.player.teamImage.PlayerImageService;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    private final StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate;
    private final PlayerImageService playerImageService;
    private final PlayerRepository playerRepository;

    @Builder
    public PlayerService(StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate, PlayerImageService playerImageService
            , PlayerRepository playerRepository){
        this.teamDetailsTemplate = teamDetailsTemplate;
        this.playerImageService = playerImageService;
        this.playerRepository = playerRepository;
    }

    @SkipLog
    public Player getOrCreatePlayerByRapidId(String rapidId) {
        if(!playerRepository.existsByRapidPlayerId(rapidId)){
            Player player = teamDetailsTemplate.execute(rapidId).toPlayer();
            String path = playerImageService.fetchImage(rapidId);
            player.updateProfileImage(path);
            try{
                return playerRepository.save(player);
            }catch(DataIntegrityViolationException e){
                return playerRepository.findByRapidPlayerId(rapidId).orElseThrow();
            }
        }

        return playerRepository.findByRapidPlayerId(rapidId).get();
    }


}
