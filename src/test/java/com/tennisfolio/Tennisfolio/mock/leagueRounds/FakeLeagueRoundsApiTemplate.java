package com.tennisfolio.Tennisfolio.mock.leagueRounds;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;

import java.util.List;

public class FakeLeagueRoundsApiTemplate extends StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> {
    public FakeLeagueRoundsApiTemplate(ApiCaller apiCaller, ResponseParser<List<LeagueRoundsDTO>> parser, EntityMapper<List<LeagueRoundsDTO>, List<Round>> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, endpoint);
    }
}
