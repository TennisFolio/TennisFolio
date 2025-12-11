package com.tennisfolio.Tennisfolio.infrastructure.worker.Statistic;

import com.tennisfolio.Tennisfolio.infrastructure.worker.GenericBatchWorker;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticBatchWorkerConfig {

    private static final int STATISTIC_BATCH_LIMIT = 500;
    private static final int STATISTIC_QUEUE_CAPACITY = 2000;

    private final StatisticBatchPipeline statisticBatchPipeline;
    private final MeterRegistry meterRegistry;

    public StatisticBatchWorkerConfig(StatisticBatchPipeline statisticBatchPipeline, MeterRegistry meterRegistry) {
        this.statisticBatchPipeline = statisticBatchPipeline;
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public GenericBatchWorker<Statistic> statisticBatchWorker(StatisticRepository statisticRepository){
        return new GenericBatchWorker<>(
                statisticBatchPipeline::runBatch,
                STATISTIC_BATCH_LIMIT,
                STATISTIC_QUEUE_CAPACITY,
                meterRegistry
        );
    }


}
