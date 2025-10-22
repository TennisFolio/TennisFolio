package com.tennisfolio.Tennisfolio.ranking.api;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchExecutor;
import com.tennisfolio.Tennisfolio.ranking.application.RankingSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingScheduler {
    private final BatchExecutor batchExecutor;
    private final RankingSyncService rankingSyncService;

    public RankingScheduler(BatchExecutor batchExecutor, RankingSyncService rankingSyncService) {
        this.batchExecutor = batchExecutor;
        this.rankingSyncService = rankingSyncService;
    }

    @Scheduled(cron = "0 30 9 * * MON", zone = "Asia/Seoul")
    public void autoRunAtpRankingBatch(){
        batchExecutor.run("AtpRankingSync", rankingSyncService::saveAtpRanking);
    }
}
