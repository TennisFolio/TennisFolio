package com.tennisfolio.Tennisfolio.mock.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;

public class FakeLeagueEventsByRoundApiTemplate extends StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> {
    public FakeLeagueEventsByRoundApiTemplate(ApiCaller apiCaller, ResponseParser<List<LeagueEventsByRoundDTO>> parser, EntityMapper<List<LeagueEventsByRoundDTO>, List<Match>> mapper, ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
