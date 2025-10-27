package com.tennisfolio.Tennisfolio.mock.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;

public class FakeLeagueSeasonInfoApiTemplate extends StrategyApiTemplate<LeagueSeasonInfoDTO, Season> {
    public FakeLeagueSeasonInfoApiTemplate(ApiCaller apiCaller, ResponseParser<LeagueSeasonInfoDTO> parser, EntityMapper<LeagueSeasonInfoDTO, Season> mapper, ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
