package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;


import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.match.domain.Match;

public class EventAssemble implements EntityAssemble<EventDTO, Match> {
    @Override
    public Match assemble(EventDTO dto, Object... params) {
        return null;
    }
}
