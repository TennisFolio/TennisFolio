package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsTemplate extends StrategyApiTemplate<LeagueDetailsDTO, Tournament> {

    public LeagueDetailsTemplate(
              ResponseParser<LeagueDetailsDTO> leagueDetailsParser
            , EntityMapper<LeagueDetailsDTO, Tournament> leagueDetailsEntityMapper
            ) {
        super(leagueDetailsParser, leagueDetailsEntityMapper, RapidApi.LEAGUEDETAILS);
    }

}
