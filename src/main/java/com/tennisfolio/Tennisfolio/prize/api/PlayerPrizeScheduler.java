package com.tennisfolio.Tennisfolio.prize.api;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchExecutor;
import com.tennisfolio.Tennisfolio.prize.application.PrizeSyncService;
import com.tennisfolio.Tennisfolio.ranking.application.RankingSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlayerPrizeScheduler {
    private final BatchExecutor batchExecutor;
    private final PrizeSyncService prizeSyncService;

    public PlayerPrizeScheduler(BatchExecutor batchExecutor, PrizeSyncService prizeSyncService) {
        this.batchExecutor = batchExecutor;
        this.prizeSyncService = prizeSyncService;
    }

    //@Scheduled(cron = "0 10 21 * * TUE", zone = "Asia/Seoul")
    public void autoSavePlayerPrize(){
        batchExecutor.run("PlayerPrizeSync", prizeSyncService::savePlayerPrize);
    }
}
