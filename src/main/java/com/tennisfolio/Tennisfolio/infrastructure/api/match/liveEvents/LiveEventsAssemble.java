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

    public LiveEventsAssemble() {

    }

    @Override
    public List<LiveMatchResponse> assemble(List<LiveEventsApiDTO> dto, Object... params) {
        if(dto == null || dto.isEmpty()){
            return List.of();
        }
        // 카테고리 선정
        dto = dto.stream().filter(p -> p.isSupportedEvent()).toList();
        
        return dto.stream()
                .map(this::getLiveMatchResponse)
                .collect(Collectors.toList());

    }

    private LiveMatchResponse getLiveMatchResponse(LiveEventsApiDTO dto){

        return new LiveMatchResponse(dto);
    }
}
