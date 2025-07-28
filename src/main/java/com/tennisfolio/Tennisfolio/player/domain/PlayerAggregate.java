package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlayerAggregate {
    private Player player;
    private PlayerPrize playerPrize;

    public PlayerAggregate(Player player, PlayerPrize playerPrize){
        this.player = player;
        this.playerPrize = playerPrize;
    }

    public Player toPlayer() {
        player.updatePrize(playerPrize);
        return this.player;
    }
}
