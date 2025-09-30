package com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventsStatisticsEntityMapper implements EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> {
    private final EntityAssemble<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsAssemble;

    public EventsStatisticsEntityMapper(EntityAssemble<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsAssemble) {
        this.eventsStatisticsAssemble = eventsStatisticsAssemble;
    }

    @Override
    public List<Statistic> map(List<EventsStatisticsDTO> dto, Object... params) {
        return eventsStatisticsAssemble.assemble(dto, params);
    }
}
