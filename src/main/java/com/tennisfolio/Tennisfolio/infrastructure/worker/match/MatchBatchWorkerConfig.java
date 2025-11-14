package com.tennisfolio.Tennisfolio.infrastructure.worker.match;

import com.tennisfolio.Tennisfolio.infrastructure.worker.GenericBatchWorker;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchBatchWorkerConfig {

    private static final int MATCH_BATCH_LIMIT = 500;

    @Bean
    public GenericBatchWorker<Match> matchBatchWorker(MatchRepository matchRepository){
        return new GenericBatchWorker<>(
                matchRepository::saveAll,
                MATCH_BATCH_LIMIT
        );
    }
}
