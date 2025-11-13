package com.tennisfolio.Tennisfolio.mock.event;

import com.tennisfolio.Tennisfolio.fixtures.EventFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.event.EventDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules.EventSchedulesDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;

public class FakeEventMapper implements EntityMapper<EventDTO, Match> {
    @Override
    public Match map(EventDTO dto, Object... params) {
        return EventFixtures.EndEvent();
    }
}
