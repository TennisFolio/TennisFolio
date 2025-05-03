package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerPrize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerAggregate {
    private Player player;
    private PlayerPrize prize;

    public PlayerAggregate(Player player, PlayerPrize prize){
        this.player = player;
        this.prize = prize;
    }
}
