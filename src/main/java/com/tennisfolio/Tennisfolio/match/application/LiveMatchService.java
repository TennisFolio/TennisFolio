package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.aop.SkipLog;
import com.tennisfolio.Tennisfolio.exception.LiveMatchNotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
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

    private final PlayerService playerService;
    private final StrategyApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate;

    public LiveMatchService(PlayerService playerService, StrategyApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate) {
        this.playerService = playerService;
        this.liveEventsTemplate = liveEventsTemplate;
    }

    public List<LiveMatchResponse> getATPLiveEvents() {
        List<LiveEventsApiDTO> apiDTO = liveEventsTemplate.executeWithoutSave("");

        if(apiDTO == null || apiDTO.isEmpty()){
            log.info("현재 진행중인 경기가 없습니다.");
        }

        return  apiDTO.stream()
                .filter(dto -> dto.getTournament().getCategory().isAtp())
                .map(this::getLiveMatchResponse)
                .collect(Collectors.toList());
    }

    public List<LiveMatchResponse> getWTALiveEvents(){
        List<LiveEventsApiDTO> apiDTO = liveEventsTemplate.executeWithoutSave("");
        if(apiDTO == null || apiDTO.isEmpty()){
            log.info("현재 진행중인 경기가 없습니다.");
        }

        return apiDTO.stream()
                .filter(dto -> dto.getTournament().getCategory().isWta())
                .map(this::getLiveMatchResponse)
                .collect(Collectors.toList());
    }


    public LiveMatchResponse getLiveEvent(String rapidMatchId) {
        List<LiveEventsApiDTO> apiDTO = liveEventsTemplate.executeWithoutSave("");

        return apiDTO.stream()
                .map(this::getLiveMatchResponse)
                .filter(dto -> rapidMatchId.equals(dto.getRapidId()))
                .findFirst()
                .orElseThrow(() -> new LiveMatchNotFoundException(ExceptionCode.NOT_FOUND));

    }

    private LiveMatchResponse getLiveMatchResponse(LiveEventsApiDTO dto){
        Player homePlayer = playerService.getOrCreatePlayerByRapidId(dto.getHomeTeam().getRapidPlayerId());
        Player awayPlayer = playerService.getOrCreatePlayerByRapidId(dto.getAwayTeam().getRapidPlayerId());
        return new LiveMatchResponse(dto, homePlayer, awayPlayer);
    }
}
