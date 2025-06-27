package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics;

import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStatisticsTemplate extends StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> {

    public EventStatisticsTemplate(
              ResponseParser<List<EventsStatisticsDTO>> eventsStatisticsResponseParser
            , EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsEntityMapper
            , EntitySaver<List<Statistic>> eventsStatisticEntitySaver) {
        super(eventsStatisticsResponseParser, eventsStatisticsEntityMapper, eventsStatisticEntitySaver, RapidApi.EVENTSTATISTICS);

    }
}
