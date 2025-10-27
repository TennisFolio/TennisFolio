package com.tennisfolio.Tennisfolio.mock.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;

public class FakeTeamDetailsApiTemplate extends StrategyApiTemplate<TeamDetailsApiDTO, PlayerAggregate> {
    public FakeTeamDetailsApiTemplate(ApiCaller apiCaller, ResponseParser<TeamDetailsApiDTO> parser,
                                      EntityMapper<TeamDetailsApiDTO, PlayerAggregate> mapper,
                                      ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
