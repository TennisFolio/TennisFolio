package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.stereotype.Component;

@Component
public class EventTemplate extends StrategyApiTemplate<EventDTO, Match> {
    public EventTemplate(ApiCaller apiCaller, ResponseParser<EventDTO> parser, EntityMapper<EventDTO, Match> mapper, ApiCallCounter apiCallCounter) {
        super(apiCaller, parser, mapper, apiCallCounter, RapidApi.EVENT);
    }
}
