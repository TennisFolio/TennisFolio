package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerPrize;
import org.springframework.stereotype.Component;

@Component
public class PlayerAssemble implements EntityAssemble<TeamDetailsApiDTO, PlayerAggregate> {
    @Override
    public PlayerAggregate assemble(TeamDetailsApiDTO dto, Object... params) {
        Player player = new Player(dto);
        PlayerPrize playerPrize = null;

        if(dto.getPrizeCurrent() != null || dto.getPrizeTotal() != null){
            playerPrize = new PlayerPrize(dto, player);
        }


        return new PlayerAggregate(player, playerPrize);
    }
}
