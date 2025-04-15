package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsMapper implements Mapper<TeamDetailsApiDTO, Player> {
    @Override
    public Player map(TeamDetailsApiDTO dto) {
        return new Player(dto);
    }
}
