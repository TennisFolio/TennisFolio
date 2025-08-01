package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsTemplate extends StrategyApiTemplate<LeagueDetailsDTO, Tournament> {

    public LeagueDetailsTemplate(
              ApiCaller apiCaller
            , ResponseParser<LeagueDetailsDTO> leagueDetailsParser
            , EntityMapper<LeagueDetailsDTO, Tournament> leagueDetailsEntityMapper
            ) {
        super(apiCaller, leagueDetailsParser, leagueDetailsEntityMapper, RapidApi.LEAGUEDETAILS);
    }

}
