package com.tennisfolio.Tennisfolio.match.dto;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class LiveMatchResponse {
    private String rapidId;
    private String tournamentName;
    private String seasonName;
    private String roundName;
    private String status;
    private LiveMatchPlayerResponse homePlayer;
    private LiveMatchPlayerResponse awayPlayer;
    private LiveMatchScoreResponse homeScore;
    private LiveMatchScoreResponse awayScore;
    private LiveMatchTimeResponse time;



    public LiveMatchResponse(LiveEventsApiDTO dto, Player homePlayer, Player awayPlayer){
        this.rapidId = dto.getRapidId();
        this.tournamentName = dto.getTournament().getName();
        this.seasonName = dto.getSeason().getName();
        this.roundName = dto.getRound().getName();
        this.status = dto.getStatus().getDescription();
        this.homePlayer = new LiveMatchPlayerResponse(dto.getHomeTeam(), homePlayer);
        this.awayPlayer = new LiveMatchPlayerResponse(dto.getAwayTeam(), awayPlayer);
        this.homeScore = new LiveMatchScoreResponse(dto.getHomeScore());
        this.awayScore = new LiveMatchScoreResponse(dto.getAwayScore());
        this.time = new LiveMatchTimeResponse(dto);
    }


}

