package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsTemplate;
import com.tennisfolio.Tennisfolio.match.response.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService{

    private final AbstractApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate;
    private final PlayerService playerService;

    public MatchServiceImpl(AbstractApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate, PlayerService playerService){
        this.liveEventsTemplate = liveEventsTemplate;
        this.playerService = playerService;
    }
    @Override
    public List<LiveMatchResponse> getLiveEvents() {
        List<LiveEventsApiDTO> apiDTO = liveEventsTemplate.executeWithoutSave("");
        return apiDTO.stream().map(dto -> {
            Player homePlayer = playerService.getOrCreatePlayerByRapidId(dto.getHomeTeam().getRapidPlayerId());
            Player awayPlayer = playerService.getOrCreatePlayerByRapidId(dto.getAwayTeam().getRapidPlayerId());
            return new LiveMatchResponse(dto, homePlayer, awayPlayer);
        }).collect(Collectors.toList());

    }
}
