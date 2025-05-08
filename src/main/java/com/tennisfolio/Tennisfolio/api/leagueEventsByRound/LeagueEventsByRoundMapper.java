package com.tennisfolio.Tennisfolio.api.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundMapper implements Mapper<List<LeagueEventsByRoundDTO>, List<Match>> {
    private final EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundAssemble;

    public LeagueEventsByRoundMapper(EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundAssemble) {
        this.leagueEventsByRoundAssemble = leagueEventsByRoundAssemble;
    }

    @Override
    public List<Match> map(List<LeagueEventsByRoundDTO> dto, Object... params) {
        return leagueEventsByRoundAssemble.assemble(dto, params);
    }
}
