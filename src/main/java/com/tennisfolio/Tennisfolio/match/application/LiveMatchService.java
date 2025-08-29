package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.aop.SkipLog;
import com.tennisfolio.Tennisfolio.exception.LiveMatchNotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveMatchService {

    private final StrategyApiTemplate<List<LiveEventsApiDTO>, List<LiveMatchResponse>> liveEventsTemplate;

    public LiveMatchService( StrategyApiTemplate<List<LiveEventsApiDTO>, List<LiveMatchResponse>> liveEventsTemplate) {
        this.liveEventsTemplate = liveEventsTemplate;
    }

    public List<LiveMatchResponse> getATPLiveEvents() {
        return liveEventsTemplate.execute("")
                .stream()
                .filter(LiveMatchResponse::isAtp)
                .collect(Collectors.toList());

    }

    public List<LiveMatchResponse> getWTALiveEvents(){
        return liveEventsTemplate.execute("")
                .stream()
                .filter(LiveMatchResponse::isWta)
                .collect(Collectors.toList());
    }


    public LiveMatchResponse getLiveEvent(String rapidMatchId) {

        return liveEventsTemplate.execute("")
                .stream()
                .filter(response -> rapidMatchId.equals(response.getRapidId()))
                .findFirst()
                .orElseThrow(() -> new LiveMatchNotFoundException(ExceptionCode.NOT_FOUND));

    }

}
