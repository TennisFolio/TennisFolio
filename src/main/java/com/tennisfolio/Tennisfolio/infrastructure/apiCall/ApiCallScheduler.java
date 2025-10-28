package com.tennisfolio.Tennisfolio.infrastructure.apiCall;

import com.tennisfolio.Tennisfolio.infrastructure.batchlog.BatchExecutor;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApiCallScheduler {
    private final BatchExecutor batchExecutor;
    private final ApiCallFlushTask apiCallFlushTask;

    public ApiCallScheduler(BatchExecutor batchExecutor, ApiCallFlushTask apiCallFlushTask) {
        this.batchExecutor = batchExecutor;
        this.apiCallFlushTask = apiCallFlushTask;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void run(){
        batchExecutor.run("apiCallCount", apiCallFlushTask::flushDailyApiCounts);
    }
}
