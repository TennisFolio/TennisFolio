package com.tennisfolio.Tennisfolio.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchScheduleResponse {
    private Long seasonId;
    private Long roundId;
    private String roundSlug;
    private Long matchId;
    private String rapidMatchId;
    private Long homeScore;
    private Long awayScore;
    private Long homePlayerId;
    private String homePlayerName;
    private String homePlayerNameKr;
    private Long awayPlayerId;
    private String awayPlayerName;
    private String awayPlayerNameKr;
    private String status;
    private String startTimestamp;
    private String winner;

    @Builder
    public MatchScheduleResponse(
            Long seasonId,
            Long roundId,
            String roundSlug,
            Long matchId,
            String rapidMatchId,
            Long homeScore,
            Long awayScore,
            Long homePlayerId,
            String homePlayerName,
            String homePlayerNameKr,
            Long awayPlayerId,
            String awayPlayerName,
            String awayPlayerNameKr,
            String status,
            String startTimestamp,
            String winner
    ) {
        this.seasonId = seasonId;
        this.roundId = roundId;
        this.roundSlug = roundSlug;
        this.matchId = matchId;
        this.rapidMatchId = rapidMatchId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homePlayerId = homePlayerId;
        this.homePlayerName = homePlayerName;
        this.homePlayerNameKr = homePlayerNameKr;
        this.awayPlayerId = awayPlayerId;
        this.awayPlayerName = awayPlayerName;
        this.awayPlayerNameKr = awayPlayerNameKr;
        this.status = status;
        this.startTimestamp = startTimestamp;
        this.winner = winner;
    }
}
