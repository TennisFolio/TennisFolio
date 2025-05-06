package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsMapper implements Mapper<TeamDetailsApiDTO, PlayerAggregate> {
    private final EntityAssemble<TeamDetailsApiDTO, PlayerAggregate> assembler;

    public TeamDetailsMapper( @Qualifier("playerAssemble") EntityAssemble<TeamDetailsApiDTO, PlayerAggregate> assembler) {
        this.assembler = assembler;
    }

    @Override
    public PlayerAggregate map(TeamDetailsApiDTO dto, Object... params) {
        return assembler.assemble(dto);
    }
}
