package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoTemplate extends StrategyApiTemplate<LeagueSeasonInfoDTO, Season> {

    public LeagueSeasonInfoTemplate(
             ResponseParser<LeagueSeasonInfoDTO> leagueSeasonInfoParser
            , EntityMapper<LeagueSeasonInfoDTO, Season> leagueSeasonInfoEntityMapper
            , EntitySaver<Season> leagueSeasonInfoEntitySaver
            ) {
        super(leagueSeasonInfoParser, leagueSeasonInfoEntityMapper, leagueSeasonInfoEntitySaver, RapidApi.LEAGUESEASONINFO);

    }
}
