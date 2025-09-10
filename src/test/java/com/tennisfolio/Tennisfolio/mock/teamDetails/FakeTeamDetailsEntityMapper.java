package com.tennisfolio.Tennisfolio.mock.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;

public class FakeTeamDetailsEntityMapper implements EntityMapper<TeamDetailsApiDTO, PlayerAggregate> {

    @Override
    public PlayerAggregate map(TeamDetailsApiDTO dto, Object... params) {
        Player player =  Player.builder()
                               .playerId(1L)
                               .rapidPlayerId("1")
                               .playerName("Alcaraz")
                               .build();
        PlayerPrize playerPrize = PlayerPrize.builder()
                .prizeId(1L)
                .prizeCurrentAmount(100L)
                .prizeCurrentCurrency("EUR")
                .prizeTotalAmount(1000L)
                .prizeTotalCurrency("EUR")
                .build();

        return new PlayerAggregate(player, playerPrize);
    }
}
