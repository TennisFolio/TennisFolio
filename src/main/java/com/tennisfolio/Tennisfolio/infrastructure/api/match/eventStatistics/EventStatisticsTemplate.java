package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.StatisticEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStatisticsTemplate extends StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> {

    public EventStatisticsTemplate(
              ApiCaller apiCaller
            , ResponseParser<List<EventsStatisticsDTO>> eventsStatisticsResponseParser
            , EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsEntityMapper
            ) {
        super(apiCaller, eventsStatisticsResponseParser, eventsStatisticsEntityMapper,  RapidApi.EVENTSTATISTICS);

    }
}
