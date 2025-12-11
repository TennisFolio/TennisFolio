package com.tennisfolio.Tennisfolio.infrastructure.worker.Statistic;

import com.tennisfolio.Tennisfolio.infrastructure.worker.AbstractBatchPipeline;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class StatisticBatchPipeline extends AbstractBatchPipeline<Statistic> {
    private final StatisticRepository statisticRepository;

    public StatisticBatchPipeline(StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    @Override
    protected List<Statistic> enrich(List<Statistic> batch) {
        return batch;
    }

    @Override
    @Transactional
    protected void save(List<Statistic> batchEntities) {
        statisticRepository.saveAll(batchEntities);
    }
}
