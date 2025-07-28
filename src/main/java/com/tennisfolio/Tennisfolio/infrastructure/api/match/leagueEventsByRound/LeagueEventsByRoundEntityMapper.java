package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundEntityMapper implements EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> {
    private final EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundAssemble;

    public LeagueEventsByRoundEntityMapper(EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundAssemble) {
        this.leagueEventsByRoundAssemble = leagueEventsByRoundAssemble;
    }

    @Override
    public List<Match> map(List<LeagueEventsByRoundDTO> dto, Object... params) {
        return leagueEventsByRoundAssemble.assemble(dto, params);
    }
}
