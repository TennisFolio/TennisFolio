package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventsStatisticsMapper implements Mapper<List<EventsStatisticsDTO>, List<Statistic>> {
    private final EntityAssemble<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsAssemble;

    public EventsStatisticsMapper(EntityAssemble<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsAssemble) {
        this.eventsStatisticsAssemble = eventsStatisticsAssemble;
    }

    @Override
    public List<Statistic> map(List<EventsStatisticsDTO> dto, Object... params) {
        return eventsStatisticsAssemble.assemble(dto, params);
    }
}
