package com.tennisfolio.Tennisfolio.mock.eventstatistics;

import com.tennisfolio.Tennisfolio.fixtures.StatisticFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;

import java.util.List;

public class FakeEventsStatisticsMapper implements EntityMapper<List<EventsStatisticsDTO>, List<Statistic>> {
    @Override
    public List<Statistic> map(List<EventsStatisticsDTO> dto, Object... params) {
        String rapidMatchId = params[0].toString();

        List<Statistic> statistics = List.of(StatisticFixtures.allFirstServe(), StatisticFixtures.allSecondServe(), StatisticFixtures.firstSetReturnPointsPoints(), StatisticFixtures.firstSetDoubleFaults());

        return statistics.stream().filter(p -> p.getMatch().getRapidMatchId().equals(rapidMatchId)).toList();
    }
}
