package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LeagueSeasonsTemplate extends StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> {

    public LeagueSeasonsTemplate(
              ApiCaller apiCaller
            , ResponseParser<List<LeagueSeasonsDTO>> leagueSeasonsParser
            , EntityMapper<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsEntityMapper
            ) {
        super(apiCaller, leagueSeasonsParser, leagueSeasonsEntityMapper, RapidApi.LEAGUESEASONS);
    }
}
