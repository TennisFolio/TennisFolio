package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayerAndPrizeEntitySaver implements EntitySaver<PlayerAggregate> {

    private final PlayerJpaRepository playerJpaRepository;
    private final PlayerPrizeRepository playerPrizeRepository;

    public PlayerAndPrizeEntitySaver(PlayerJpaRepository playerJpaRepository, PlayerPrizeRepository playerPrizeRepository){
        this.playerJpaRepository = playerJpaRepository;
        this.playerPrizeRepository = playerPrizeRepository;
    }
    @Override
    public PlayerAggregate save(PlayerAggregate aggregate) {
        Player incomingPlayer = aggregate.getPlayer();

        Optional<Player> existingPlayerOpt = playerJpaRepository.findByRapidPlayerId(incomingPlayer.getRapidPlayerId());

        Player savedPlayer = existingPlayerOpt.map(existing -> {
            existing.updateFrom(incomingPlayer);
            return playerJpaRepository.save(existing);
        }).orElseGet(() -> playerJpaRepository.save(incomingPlayer));

        PlayerPrize savedPrize = null;
        PlayerPrize incomingPrize = aggregate.getPrize();
        if(incomingPrize != null){
            Optional<PlayerPrize> existingPlayerPrizeOpt = playerPrizeRepository.findById(savedPlayer.getPlayerId());

            savedPrize = existingPlayerPrizeOpt.map(existing -> {
                existing.updatePlayerPrize(incomingPrize);
                return playerPrizeRepository.save(existing);
            }).orElseGet(() -> playerPrizeRepository.save(incomingPrize));
        }
        return new PlayerAggregate(savedPlayer, savedPrize);
    }
}

