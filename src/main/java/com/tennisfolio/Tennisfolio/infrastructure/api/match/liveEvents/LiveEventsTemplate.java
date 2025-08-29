package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;


import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveEventsTemplate extends StrategyApiTemplate<List<LiveEventsApiDTO>, List<LiveMatchResponse>> {

    public LiveEventsTemplate(
            ApiCaller apiCaller,
         @Qualifier("liveEventsResponseParser") ResponseParser<List<LiveEventsApiDTO>> parser,
            EntityMapper<List<LiveEventsApiDTO>, List<LiveMatchResponse>> entityMapper) {
        super(apiCaller, parser, entityMapper,  RapidApi.LIVEEVENTS);
    }

}
