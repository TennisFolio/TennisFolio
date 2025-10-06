package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Match {

    private Long matchId;

    private Round round;

    private String rapidMatchId;

    private String homeSeed;

    private String awaySeed;

    private Long homeScore;

    private Long awayScore;

    private Player homePlayer;

    private Player awayPlayer;

    private Score homeSet;

    private Score awaySet;

    private Period periodSet;

    private String startTimeStamp;

    private String winner;

    private String status;

    public Match(String rapidMatchId, String homeSeed, String awaySeed, Long homeScore, Long awayScore
            , Round round, Player homePlayer, Player awayPlayer, Score homeSet, Score awaySet, Period periodSet, String startTimeStamp, String winner, String status){
        this.round = round;
        this.rapidMatchId = rapidMatchId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeSeed = homeSeed;
        this.awaySeed = awaySeed;
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
        this.homeSet = homeSet;
        this.awaySet = awaySet;
        this.periodSet = periodSet;
        this.startTimeStamp = startTimeStamp;
        this.winner = winner;
        this.status = status;
    }

    public void nullToZero(){
        homeSet.nullToZero();
        awaySet.nullToZero();
    }

    public void convertTime() {
        if (periodSet != null) {
            periodSet.convertPeriods();
        }
        startTimeStamp = ConversionUtil.timestampToYyyyMMddHHMMSS(startTimeStamp);
    }

}
