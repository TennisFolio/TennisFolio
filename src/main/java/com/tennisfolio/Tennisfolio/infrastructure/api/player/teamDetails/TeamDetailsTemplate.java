package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsTemplate extends StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> {


    public TeamDetailsTemplate(ApiCaller apiCaller
                               , ResponseParser<TeamDetailsApiDTO> responseParser
                               , EntityMapper<TeamDetailsApiDTO, PlayerAggregate> entityMapper
                              ){
        super(apiCaller, responseParser, entityMapper, RapidApi.TEAMDETAILS);
    }


}
