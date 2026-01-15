package com.tennisfolio.Tennisfolio.match.event.listener;

import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.event.MatchFinishedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
public class MatchStartTimeChangeEventListener {
    private final LiveMatchService liveMatchService;

    public MatchStartTimeChangeEventListener(LiveMatchService liveMatchService) {
        this.liveMatchService = liveMatchService;
    }

    @Async("eventExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handle(MatchFinishedEvent event){
        liveMatchService.changeStartTimeProc(event.rapidMatchId());
    }
}
