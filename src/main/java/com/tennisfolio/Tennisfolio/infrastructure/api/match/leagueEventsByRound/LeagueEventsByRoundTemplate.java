package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundTemplate extends StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> {

    public LeagueEventsByRoundTemplate(
              ApiCaller apiCaller
            , ResponseParser<List<LeagueEventsByRoundDTO>> leagueEventsByRoundResponseParser
            , EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundEntityMapper

            ) {
        super(apiCaller, leagueEventsByRoundResponseParser, leagueEventsByRoundEntityMapper, RapidApi.LEAGUEEVENETBYROUND);
    }
}
