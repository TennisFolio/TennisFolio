package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.aop.SkipLog;
import com.tennisfolio.Tennisfolio.exception.LiveMatchNotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveMatchService {

    private final ApiWorker apiWorker;
    private final PlayerProvider playerProvider;

    public LiveMatchService(ApiWorker apiWorker, PlayerProvider playerProvider) {

        this.apiWorker = apiWorker;
        this.playerProvider = playerProvider;
    }

    public List<LiveMatchResponse> getATPLiveEvents() {
        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        return liveMatches.stream()
                .filter(LiveMatchResponse::isAtp)
                .map(p -> {
                    String homePlayerImage = playerProvider.provide(p.getHomePlayer().getPlayerRapidId()).getImage();
                    String awayPlayerImage = playerProvider.provide(p.getAwayPlayer().getPlayerRapidId()).getImage();

                    p.setPlayerImage(homePlayerImage, awayPlayerImage);
                    return p;
                })
                .collect(Collectors.toList());


    }

    public List<LiveMatchResponse> getWTALiveEvents(){

        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);
        return liveMatches.stream()
                .filter(LiveMatchResponse::isWta)
                .map(p -> {
                    String homePlayerImage = playerProvider.provide(p.getHomePlayer().getPlayerRapidId()).getImage();
                    String awayPlayerImage = playerProvider.provide(p.getAwayPlayer().getPlayerRapidId()).getImage();

                    p.setPlayerImage(homePlayerImage, awayPlayerImage);
                    return p;
                })
                .collect(Collectors.toList());
    }


    public LiveMatchResponse getLiveEvent(String rapidMatchId) {

        List<LiveMatchResponse> liveMatches = apiWorker.process(RapidApi.LIVEEVENTS);

        return liveMatches
                .stream()
                .filter(response -> rapidMatchId.equals(response.getRapidId()))
                .findFirst()
                .orElseThrow(() -> new LiveMatchNotFoundException(ExceptionCode.NOT_FOUND));

    }

}
