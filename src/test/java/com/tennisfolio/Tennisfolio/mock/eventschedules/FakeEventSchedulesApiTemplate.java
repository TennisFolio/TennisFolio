package com.tennisfolio.Tennisfolio.mock.eventschedules;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;

public class FakeEventSchedulesApiTemplate extends StrategyApiTemplate<List<EventSchedulesDTO>, List<Match>> {
    public FakeEventSchedulesApiTemplate(ApiCaller apiCaller, ResponseParser<List<EventSchedulesDTO>> parser, EntityMapper<List<EventSchedulesDTO>, List<Match>> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, RapidApi.EVENTSCHEDULES);
    }
}
