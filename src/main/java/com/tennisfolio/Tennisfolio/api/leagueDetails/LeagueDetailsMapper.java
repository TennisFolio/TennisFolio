package com.tennisfolio.Tennisfolio.api.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsMapper implements Mapper<LeagueDetailsDTO, Tournament> {
    private final EntityAssemble<LeagueDetailsDTO, Tournament> leagueDetailsAssemble;

    public LeagueDetailsMapper(EntityAssemble<LeagueDetailsDTO, Tournament> leagueDetailsAssemble) {
        this.leagueDetailsAssemble = leagueDetailsAssemble;
    }

    @Override
    public Tournament map(LeagueDetailsDTO dto) {
        return leagueDetailsAssemble.assemble(dto);
    }
}
