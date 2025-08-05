package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoTemplate extends StrategyApiTemplate<LeagueSeasonInfoDTO, Season> {

    public LeagueSeasonInfoTemplate(
              ApiCaller apiCaller
            , ResponseParser<LeagueSeasonInfoDTO> leagueSeasonInfoParser
            , EntityMapper<LeagueSeasonInfoDTO, Season> leagueSeasonInfoEntityMapper
            ) {
        super(apiCaller, leagueSeasonInfoParser, leagueSeasonInfoEntityMapper,  RapidApi.LEAGUESEASONINFO);

    }
}
