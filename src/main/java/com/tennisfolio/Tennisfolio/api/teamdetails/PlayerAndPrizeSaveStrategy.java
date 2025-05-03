package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.player.repository.PlayerPrizeRepository;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayerAndPrizeSaveStrategy implements SaveStrategy<PlayerAggregate> {

    private final PlayerRepository playerRepository;
    private final PlayerPrizeRepository playerPrizeRepository;

    public PlayerAndPrizeSaveStrategy(PlayerRepository playerRepository, PlayerPrizeRepository playerPrizeRepository){
        this.playerRepository = playerRepository;
        this.playerPrizeRepository = playerPrizeRepository;
    }
    @Override
    public PlayerAggregate save(PlayerAggregate entity) {
        Player existingPlayer = playerRepository.findByRapidPlayerId(entity.getPlayer().getRapidPlayerId())
                .orElseGet(() -> playerRepository.save(entity.getPlayer()));

        PlayerPrize playerPrize = entity.getPrize() != null
                ? playerPrizeRepository.save(entity.getPrize())
                : null;


        return new PlayerAggregate(existingPlayer, playerPrize);
    }
}

