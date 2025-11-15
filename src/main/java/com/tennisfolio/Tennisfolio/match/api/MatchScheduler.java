package com.tennisfolio.Tennisfolio.match.api;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchExecutor;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchScheduler {
    private final BatchExecutor batchExecutor;
    private final MatchSyncService matchSyncService;
    private final LiveMatchService liveMatchService;

    public MatchScheduler(BatchExecutor batchExecutor, MatchSyncService matchSyncService, LiveMatchService liveMatchService) {
        this.batchExecutor = batchExecutor;
        this.matchSyncService = matchSyncService;
        this.liveMatchService = liveMatchService;
    }

    @Scheduled(cron= "0 0 0,12 * * *", zone = "Asia/Seoul")
    public void autoRunEventScheduleBatch(){
        batchExecutor.run("eventSchedules", matchSyncService::saveEventSchedule);
    }

    @Scheduled(cron="0,30 * * * * *", zone= "Asia/Seoul")
    public void updateLiveMatches(){
//        liveMatchService.updateLiveMatches();
    }
}
