package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.StatisticRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventsStatisticsEntitySaver implements EntitySaver<List<Statistic>> {
    private final StatisticRepository statisticRepository;

    public EventsStatisticsEntitySaver(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    public List<Statistic> save(List<Statistic> entity) {
        Match match = entity.stream()
                .findFirst()
                .get()
                .getMatch();

        List<Statistic> toSave = entity.stream()
                .filter(statistic
                        -> statisticRepository.findByMatchAndPeriodAndGroupName(match, statistic.getPeriod(), statistic.getGroupName())
                            .isEmpty()
                ).collect(Collectors.toList());


        return statisticRepository.saveAll(toSave);
    }
}
