package com.tennisfolio.Tennisfolio.match.api;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchExecutor;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchScheduler {
    private final BatchExecutor batchExecutor;
    private final MatchSyncService matchSyncService;

    public MatchScheduler(BatchExecutor batchExecutor, MatchSyncService matchSyncService) {
        this.batchExecutor = batchExecutor;
        this.matchSyncService = matchSyncService;
    }

    @Scheduled(cron= "0 0 0,12 * * *", zone = "Asia/Seoul")
    public void autoRunEventScheduleBatch(){
        batchExecutor.run("eventSchedules", matchSyncService::saveEventSchedule);
    }
}
