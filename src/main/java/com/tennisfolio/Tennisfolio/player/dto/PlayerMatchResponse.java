package com.tennisfolio.Tennisfolio.player.dto;

import com.tennisfolio.Tennisfolio.common.RoundType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PlayerMatchResponse {
    private Long categoryId;
    private String categoryName;
    private Long tournamentId;
    private String tournamentName;
    private Long seasonId;
    private String seasonName;
    private Long roundId;
    private String roundName;
    private String roundNameKr;
    private Long matchId;
    private String rapidMatchId;
    private Long homePlayerId;
    private String homePlayerName;
    private String homePlayerNameKr;
    private List<Long> homePlayerSet = new ArrayList<>();
    private List<Long> homePlayerSetTie = new ArrayList<>();
    private Long awayPlayerId;
    private String awayPlayerName;
    private String awayPlayerNameKr;
    private List<Long> awayPlayerSet = new ArrayList<>();
    private List<Long> awayPlayerSetTie = new ArrayList<>();
    private String winner;
    private String startTimestamp;

    @Builder
    public PlayerMatchResponse(Long categoryId, String categoryName, Long tournamentId, String tournamentName, Long seasonId, String seasonName,
                               Long roundId, String roundName, Long matchId, String rapidMatchId, Long homePlayerId, String homePlayerName, String homePlayerNameKr,
                               Long homeSet1, Long homeSet2, Long homeSet3, Long homeSet4, Long homeSet5,
                               Long homeSet1Tie, Long homeSet2Tie, Long homeSet3Tie, Long homeSet4Tie, Long homeSet5Tie,
                               Long awayPlayerId, String awayPlayerName, String awayPlayerNameKr,
                               Long awaySet1, Long awaySet2, Long awaySet3, Long awaySet4, Long awaySet5,
                               Long awaySet1Tie, Long awaySet2Tie, Long awaySet3Tie, Long awaySet4Tie, Long awaySet5Tie,
                               String winner, String startTimestamp){
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.seasonId = seasonId;
        this.seasonName = seasonName;
        this.roundId = roundId;
        this.roundName = roundName;
        this.roundNameKr = RoundType.fromName(roundName).getKrName();
        this.matchId = matchId;
        this.rapidMatchId = rapidMatchId;
        this.homePlayerId = homePlayerId;
        this.homePlayerName = homePlayerName;
        this.homePlayerNameKr = homePlayerNameKr;
        this.homePlayerSet.addAll(List.of(homeSet1, homeSet2, homeSet3, homeSet4, homeSet5));
        this.homePlayerSetTie.addAll(List.of(homeSet1Tie, homeSet2Tie, homeSet3Tie, homeSet4Tie, homeSet5Tie));
        this.awayPlayerId = awayPlayerId;
        this.awayPlayerName = awayPlayerName;
        this.awayPlayerNameKr = awayPlayerNameKr;
        this.awayPlayerSet.addAll(List.of(awaySet1, awaySet2, awaySet3, awaySet4, awaySet5));
        this.awayPlayerSetTie.addAll(List.of(awaySet1Tie, awaySet2Tie, awaySet3Tie, awaySet4Tie, awaySet5Tie));
        this.winner = winner;
        this.startTimestamp = startTimestamp;
    }
}
