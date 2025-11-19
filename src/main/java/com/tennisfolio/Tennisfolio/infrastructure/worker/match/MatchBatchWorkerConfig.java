package com.tennisfolio.Tennisfolio.infrastructure.worker.match;

import com.tennisfolio.Tennisfolio.infrastructure.worker.GenericBatchWorker;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchBatchWorkerConfig {

    private static final int MATCH_BATCH_LIMIT = 500;
    private static final int MATCH_QUEUE_CAPACITY = 2000; // 대략 500 * 4 라운드 정도

    private final MatchBatchPipeline matchBatchPipeline;

    public MatchBatchWorkerConfig(MatchBatchPipeline matchBatchPipeline) {
        this.matchBatchPipeline = matchBatchPipeline;
    }

    @Bean
    public GenericBatchWorker<Match> matchBatchWorker(MatchRepository matchRepository){
        return new GenericBatchWorker<>(
                matchBatchPipeline::runBatch,
                MATCH_BATCH_LIMIT,
                MATCH_QUEUE_CAPACITY
        );
    }
}
