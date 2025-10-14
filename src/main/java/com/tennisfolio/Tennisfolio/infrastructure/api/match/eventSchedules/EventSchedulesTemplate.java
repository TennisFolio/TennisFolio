package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventSchedulesTemplate extends StrategyApiTemplate<List<EventSchedulesDTO>, List<Match>> {
    public EventSchedulesTemplate(ApiCaller apiCaller, ResponseParser<List<EventSchedulesDTO>> parser, EntityMapper<List<EventSchedulesDTO>, List<Match>> mapper) {
        super(apiCaller, parser, mapper, RapidApi.EVENTSCHEDULES);
    }
}
