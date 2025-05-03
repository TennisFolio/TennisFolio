package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.EntityAssembler;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsMapper implements Mapper<TeamDetailsApiDTO, PlayerAggregate> {
    private final EntityAssembler<TeamDetailsApiDTO, PlayerAggregate> assembler;

    public TeamDetailsMapper( @Qualifier("playerAssemble") EntityAssembler<TeamDetailsApiDTO, PlayerAggregate> assembler) {
        this.assembler = assembler;
    }

    @Override
    public PlayerAggregate map(TeamDetailsApiDTO dto) {
        return assembler.assemble(dto);
    }
}
