package com.tennisfolio.Tennisfolio.matching.dto;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompetitionRankingResponse {
    private Integer rank;
    private Long competitionEntryId;
    private String playerName;
    private String gender;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Double winRate;
    private Integer rankingPoints;
    private Double rankingPointRate;
    private Integer pointsFor;
    private Integer pointsAgainst;
    private Integer pointDiff;
    private Integer tiebreakPointsFor;
    private Integer tiebreakPointsAgainst;
    private Integer tiebreakPointDiff;

    public static CompetitionRankingResponse from(
            Integer rank,
            CompetitionEntry entry,
            Integer gamesPlayed,
            Integer wins,
            Integer losses,
            Integer draws,
            Integer pointsFor,
            Integer pointsAgainst,
            Integer tiebreakPointsFor,
            Integer tiebreakPointsAgainst
    ) {
        double winRate = gamesPlayed == 0 ? 0.0 : (double) wins / gamesPlayed;
        int rankingPoints = wins * 2 + draws;
        double rankingPointRate = gamesPlayed == 0 ? 0.0 : (double) rankingPoints / gamesPlayed;

        return new CompetitionRankingResponse(
                rank,
                entry.getId(),
                entry.getPlayerName(),
                entry.getGender().name(),
                gamesPlayed,
                wins,
                losses,
                draws,
                winRate,
                rankingPoints,
                rankingPointRate,
                pointsFor,
                pointsAgainst,
                pointsFor - pointsAgainst,
                tiebreakPointsFor,
                tiebreakPointsAgainst,
                tiebreakPointsFor - tiebreakPointsAgainst
        );
    }
}
