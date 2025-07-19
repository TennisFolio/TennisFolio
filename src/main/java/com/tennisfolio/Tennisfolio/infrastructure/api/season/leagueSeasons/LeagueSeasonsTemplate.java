package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LeagueSeasonsTemplate extends StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> {

    public LeagueSeasonsTemplate(
              ResponseParser<List<LeagueSeasonsDTO>> leagueSeasonsParser
            , EntityMapper<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsEntityMapper
            ) {
        super(leagueSeasonsParser, leagueSeasonsEntityMapper, RapidApi.LEAGUESEASONS);
    }
}
