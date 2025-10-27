package com.tennisfolio.Tennisfolio.mock.eventstatistics;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.hibernate.stat.Statistics;

import java.util.List;

public class FakeEventStatisticsApiTemplate extends StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> {
    public FakeEventStatisticsApiTemplate(ApiCaller apiCaller, ResponseParser<List<EventsStatisticsDTO>> parser, EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> mapper, ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
