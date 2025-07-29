package com.tennisfolio.Tennisfolio.statistic.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.StatisticJpaRepository;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StatisticRepositoryImpl implements StatisticRepository{
    private final StatisticJpaRepository statisticJpaRepository;

    public StatisticRepositoryImpl(StatisticJpaRepository statisticJpaRepository) {
        this.statisticJpaRepository = statisticJpaRepository;
    }

    @Override
    public List<Statistic> saveAll(List<Statistic> statistics) {
        List<StatisticEntity> entities = statistics.stream().map(StatisticEntity::fromModel).toList();
        return statisticJpaRepository.saveAll(entities).stream().map(StatisticEntity::toModel).toList();
    }
}
