package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.StatisticRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventsStatisticsSaveStrategy implements SaveStrategy<List<Statistic>> {
    private final StatisticRepository statisticRepository;

    public EventsStatisticsSaveStrategy(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    public List<Statistic> save(List<Statistic> entity) {
        Match match = entity.stream()
                .findFirst()
                .get()
                .getMatch();

        List<Statistic> toSave = entity.stream().map(statistic -> {
            String period = statistic.getPeriod();
            String group = statistic.getGroupName();
            statisticRepository.findByMatchAndPeriodAndGroupName(match, period, group)
                    .ifPresent(existing -> statistic.setStatId(existing.getStatId()));

            return statistic;
        }).collect(Collectors.toList());


        return statisticRepository.saveAll(toSave);
    }
}
