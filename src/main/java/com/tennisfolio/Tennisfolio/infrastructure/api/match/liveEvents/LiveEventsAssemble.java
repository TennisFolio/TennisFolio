package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LiveEventsAssemble implements EntityAssemble<List<LiveEventsApiDTO>, List<LiveMatchResponse>> {
    private final PlayerService playerService;

    public LiveEventsAssemble(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public List<LiveMatchResponse> assemble(List<LiveEventsApiDTO> dto, Object... params) {
        if(dto == null || dto.isEmpty()){
            return List.of();
        }
        //atp, wta만 전달
        dto = dto.stream().filter(p -> p.isAtpEvent() || p.isWtaEvent()).toList();
        
        return dto.stream()
                .map(this::getLiveMatchResponse)
                .collect(Collectors.toList());

    }

    private LiveMatchResponse getLiveMatchResponse(LiveEventsApiDTO dto){
        Player homePlayer = playerService.getOrCreatePlayerByRapidId(dto.getHomeTeam().getRapidPlayerId());
        Player awayPlayer = playerService.getOrCreatePlayerByRapidId(dto.getAwayTeam().getRapidPlayerId());
        return new LiveMatchResponse(dto, homePlayer, awayPlayer);
    }
}
