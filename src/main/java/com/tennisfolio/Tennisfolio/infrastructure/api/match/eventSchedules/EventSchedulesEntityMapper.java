package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventSchedulesEntityMapper implements EntityMapper<List<EventSchedulesDTO>, List<Match>> {
    private final EntityAssemble<List<EventSchedulesDTO>, List<Match>> eventSchedulesAssemble;

    public EventSchedulesEntityMapper(EntityAssemble<List<EventSchedulesDTO>, List<Match>> eventSchedulesAssemble) {
        this.eventSchedulesAssemble = eventSchedulesAssemble;
    }

    @Override
    public List<Match> map(List<EventSchedulesDTO> dto, Object... params) {
        return eventSchedulesAssemble.assemble(dto, params);
    }
}
