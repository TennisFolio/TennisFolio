package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsEntityMapper implements EntityMapper<TeamDetailsApiDTO, PlayerAggregate> {

    @Override
    public PlayerAggregate map(TeamDetailsApiDTO dto, Object... params) {
        Player player = new Player(dto);
        PlayerPrize prize = new PlayerPrize(dto, player);

        return new PlayerAggregate(player, prize);

    }
}
