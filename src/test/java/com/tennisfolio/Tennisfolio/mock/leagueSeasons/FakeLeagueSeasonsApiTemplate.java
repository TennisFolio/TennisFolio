package com.tennisfolio.Tennisfolio.mock.leagueSeasons;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;

public class FakeLeagueSeasonsApiTemplate extends StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> {
    public FakeLeagueSeasonsApiTemplate(ApiCaller apiCaller, ResponseParser<List<LeagueSeasonsDTO>> parser, EntityMapper<List<LeagueSeasonsDTO>, List<Season>> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, endpoint);
    }
}
