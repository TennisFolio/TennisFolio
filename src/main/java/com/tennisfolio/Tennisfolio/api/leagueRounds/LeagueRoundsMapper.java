package com.tennisfolio.Tennisfolio.api.leagueRounds;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LeagueRoundsMapper implements Mapper<List<LeagueRoundsDTO>, List<Round>> {
    private final EntityAssemble<List<LeagueRoundsDTO>, List<Round>> leagueRoundsAssemble;

    public LeagueRoundsMapper(EntityAssemble<List<LeagueRoundsDTO>, List<Round>> leagueRoundsAssemble) {
        this.leagueRoundsAssemble = leagueRoundsAssemble;
    }

    @Override
    public List<Round> map(List<LeagueRoundsDTO> dto, Object... params) {
        return leagueRoundsAssemble.assemble(dto, params);
    }
}
