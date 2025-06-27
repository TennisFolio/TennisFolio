package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlayerAggregate {
    private Player player;
    private PlayerPrize prize;

    public PlayerAggregate(Player player, PlayerPrize prize){
        this.player = player;
        this.prize = prize;
    }

    public Player toPlayer() {
        player.updatePrize(prize);
        return this.player;
    }
}
