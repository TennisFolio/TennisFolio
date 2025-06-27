package com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LeagueRoundsEntityMapper implements EntityMapper<List<LeagueRoundsDTO>, List<Round>> {
    private final EntityAssemble<List<LeagueRoundsDTO>, List<Round>> leagueRoundsAssemble;

    public LeagueRoundsEntityMapper(EntityAssemble<List<LeagueRoundsDTO>, List<Round>> leagueRoundsAssemble) {
        this.leagueRoundsAssemble = leagueRoundsAssemble;
    }

    @Override
    public List<Round> map(List<LeagueRoundsDTO> dto, Object... params) {
        return leagueRoundsAssemble.assemble(dto, params);
    }
}
