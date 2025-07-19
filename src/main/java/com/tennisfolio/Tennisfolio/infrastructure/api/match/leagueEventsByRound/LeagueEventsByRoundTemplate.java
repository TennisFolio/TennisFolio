package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundTemplate extends StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> {

    public LeagueEventsByRoundTemplate(
              ResponseParser<List<LeagueEventsByRoundDTO>> leagueEventsByRoundResponseParser
            , EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundEntityMapper

            ) {
        super(leagueEventsByRoundResponseParser, leagueEventsByRoundEntityMapper, RapidApi.LEAGUEEVENETBYROUND);
    }
}
