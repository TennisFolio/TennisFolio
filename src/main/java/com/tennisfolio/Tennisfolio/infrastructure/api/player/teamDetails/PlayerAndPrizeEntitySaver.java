package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeRepository;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayerAndPrizeEntitySaver implements EntitySaver<PlayerAggregate> {

    private final PlayerRepository playerRepository;
    private final PlayerPrizeRepository playerPrizeRepository;

    public PlayerAndPrizeEntitySaver(PlayerRepository playerRepository, PlayerPrizeRepository playerPrizeRepository){
        this.playerRepository = playerRepository;
        this.playerPrizeRepository = playerPrizeRepository;
    }
    @Override
    public PlayerAggregate save(PlayerAggregate aggregate) {
        Player incomingPlayer = aggregate.getPlayer();

        Optional<Player> existingPlayerOpt = playerRepository.findByRapidPlayerId(incomingPlayer.getRapidPlayerId());

        Player savedPlayer = existingPlayerOpt.map(existing -> {
            existing.updateFrom(incomingPlayer);
            return playerRepository.save(existing);
        }).orElseGet(() -> playerRepository.save(incomingPlayer));

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

