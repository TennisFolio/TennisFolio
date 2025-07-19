package com.tennisfolio.Tennisfolio.infrastructure.api.player.teamDetails;

import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsTemplate extends StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> {


    public TeamDetailsTemplate(
                               ResponseParser<TeamDetailsApiDTO> responseParser,
                               EntityMapper<TeamDetailsApiDTO, PlayerAggregate> entityMapper
                              ){
        super(responseParser, entityMapper, RapidApi.TEAMDETAILS);
    }


}
