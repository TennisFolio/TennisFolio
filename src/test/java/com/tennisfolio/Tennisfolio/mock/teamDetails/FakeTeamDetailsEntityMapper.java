package com.tennisfolio.Tennisfolio.mock.teamDetails;

import com.tennisfolio.Tennisfolio.fixtures.PlayerFixtures;
import com.tennisfolio.Tennisfolio.fixtures.PlayerPrizeFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;

import java.util.List;

public class FakeTeamDetailsEntityMapper implements EntityMapper<TeamDetailsApiDTO, PlayerAggregate> {

    @Override
    public PlayerAggregate map(TeamDetailsApiDTO dto, Object... params) {
        String rapidPlayerId = params[0].toString();

        List<PlayerAggregate> playerAggregateList = List.of(new PlayerAggregate(PlayerFixtures.alcaraz(), PlayerPrizeFixtures.alcarazUpdate()),
                new PlayerAggregate(PlayerFixtures.nadal(), PlayerPrizeFixtures.nadalUpdate()),
                new PlayerAggregate(PlayerFixtures.sinner(), PlayerPrizeFixtures.sinnerUpdate()),
                new PlayerAggregate(PlayerFixtures.federer(), PlayerPrizeFixtures.federer()));


        return playerAggregateList.stream()
                .filter(p -> p.toPlayer().getRapidPlayerId().equals(rapidPlayerId))
                .findFirst()
                .orElseGet(null);
    }
}
