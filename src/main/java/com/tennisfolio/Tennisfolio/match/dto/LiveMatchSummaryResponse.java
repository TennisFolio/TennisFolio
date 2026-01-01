package com.tennisfolio.Tennisfolio.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LiveMatchSummaryResponse {
    private LiveMatchPlayerResponse homePlayer;
    private LiveMatchPlayerResponse awayPlayer;
    private String category;
    private String tournamentName;
    private String seasonName;
    private String roundName;
    private String status;
    private Long homeScore;
    private Long awayScore;

    public LiveMatchSummaryResponse(LiveMatchResponse liveMatchResponse){
        this.homePlayer = liveMatchResponse.getHomePlayer();
        this.awayPlayer = liveMatchResponse.getAwayPlayer();
        this.category = liveMatchResponse.getCategory();
        this.tournamentName = liveMatchResponse.getTournamentName();
        this.seasonName = liveMatchResponse.getSeasonName();
        this.roundName = liveMatchResponse.getRoundName();
        this.status = liveMatchResponse.getStatus();
        this.homeScore = liveMatchResponse.getHomeScore().getCurrent();
        this.awayScore = liveMatchResponse.getAwayScore().getCurrent();
    }
}
