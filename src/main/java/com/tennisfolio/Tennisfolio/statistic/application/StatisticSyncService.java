package com.tennisfolio.Tennisfolio.statistic.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.MatchJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.StatisticJpaRepository;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatisticSyncService {

    private final StrategyApiTemplate<List<EventsStatisticsDTO>, List<Statistic>> eventsStatisticsTemplate;
    private final MatchRepository matchRepository;
    private final StatisticRepository statisticRepository;
    public StatisticSyncService(StrategyApiTemplate<List<EventsStatisticsDTO>,
            List<Statistic>> eventsStatisticsTemplate,
            MatchRepository matchRepository,
            StatisticRepository statisticRepository) {
        this.eventsStatisticsTemplate = eventsStatisticsTemplate;
        this.matchRepository = matchRepository;
        this.statisticRepository = statisticRepository;
    }

    public void saveStatisticList() {
        List<Statistic> statistics = matchRepository.findAll()
                .stream()
                .map(match -> {
                    String rapidMatchId = match.getRapidMatchId();
                    return eventsStatisticsTemplate.execute(rapidMatchId);
                }).flatMap(list -> list != null ? list.stream() : Stream.empty())
                .collect(Collectors.toList());
        statisticRepository.saveAll(statistics);

    }
}
