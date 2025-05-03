package com.tennisfolio.Tennisfolio.match.response;

import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LiveMatchResponse {
    private String rapidId;
    private String tournamentName;
    private String seasonName;
    private String roundName;
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
        this.homePlayer = new LiveMatchPlayerResponse(dto.getHomeTeam(), homePlayer);
        this.awayPlayer = new LiveMatchPlayerResponse(dto.getAwayTeam(), awayPlayer);
        this.homeScore = new LiveMatchScoreResponse(dto.getHomeScore());
        this.awayScore = new LiveMatchScoreResponse(dto.getAwayScore());
        this.time = new LiveMatchTimeResponse(dto);
    }


}

