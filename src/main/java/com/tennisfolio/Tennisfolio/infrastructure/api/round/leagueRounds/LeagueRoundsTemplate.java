package com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueRoundsTemplate extends StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> {
    public LeagueRoundsTemplate(ApiCaller apiCaller
            , ResponseParser<List<LeagueRoundsDTO>> leagueRoundsResponseParser
            , EntityMapper<List<LeagueRoundsDTO>, List<Round>> leagueRoundsEntityMapper
            , ApiCallCounter apiCallCounter) {

        super(apiCaller, leagueRoundsResponseParser, leagueRoundsEntityMapper,apiCallCounter, RapidApi.LEAGUEROUNDS);
    }
}
