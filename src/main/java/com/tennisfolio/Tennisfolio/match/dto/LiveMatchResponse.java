package com.tennisfolio.Tennisfolio.match.dto;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LiveMatchResponse {
    private String rapidId;
    private String category;
    private String tournamentName;
    private String seasonName;
    private String roundName;
    private String status;

    private LiveMatchPlayerResponse homePlayer;
    private LiveMatchPlayerResponse awayPlayer;
    private LiveMatchScoreResponse homeScore;
    private LiveMatchScoreResponse awayScore;
    private LiveMatchTimeResponse time;



    public LiveMatchResponse(LiveEventsApiDTO dto){
        this.rapidId = dto.getRapidId();
        this.category = dto.getTournament().getCategory().getSlug();
        this.tournamentName = dto.getTournament().getName();
        this.seasonName = dto.getSeason().getName();
        this.roundName = dto.getRound() != null ? dto.getRound().getName() : null;
        this.status = dto.getStatus().getDescription();
        this.homePlayer = new LiveMatchPlayerResponse(dto.getHomeTeam());
        this.awayPlayer = new LiveMatchPlayerResponse(dto.getAwayTeam());
        this.homeScore = new LiveMatchScoreResponse(dto.getHomeScore());
        this.awayScore = new LiveMatchScoreResponse(dto.getAwayScore());
        this.time = new LiveMatchTimeResponse(dto);
    }

    public void setPlayerImage(String homePlayerImage, String awayPlayerImage){
        this.homePlayer.setPlayerInfo(homePlayerImage);
        this.awayPlayer.setPlayerInfo(awayPlayerImage);
    }

    public boolean isAtp(){
        return "atp".equals(category);
    }

    public boolean isWta(){
        return "wta".equals(category);
    }


}

