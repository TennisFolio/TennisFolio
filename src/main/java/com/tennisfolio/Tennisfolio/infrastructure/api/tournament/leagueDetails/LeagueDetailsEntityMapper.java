package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsEntityMapper implements EntityMapper<LeagueDetailsDTO, Tournament> {
    private final EntityAssemble<LeagueDetailsDTO, Tournament> leagueDetailsAssemble;

    public LeagueDetailsEntityMapper(EntityAssemble<LeagueDetailsDTO, Tournament> leagueDetailsAssemble) {
        this.leagueDetailsAssemble = leagueDetailsAssemble;
    }

    @Override
    public Tournament map(LeagueDetailsDTO dto, Object... params) {
        return leagueDetailsAssemble.assemble(dto);
    }
}
