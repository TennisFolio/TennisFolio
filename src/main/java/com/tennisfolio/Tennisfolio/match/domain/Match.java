package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
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

    private String startTimestamp;

    private String winner;

    private String status;

    public Match(String rapidMatchId, String homeSeed, String awaySeed, Long homeScore, Long awayScore
            , Round round, Player homePlayer, Player awayPlayer, Score homeSet, Score awaySet, Period periodSet, String startTimestamp, String winner, String status){
        this.round = round;
        this.rapidMatchId = rapidMatchId;
        this.homeScore = homeScore == null? 0L : homeScore;
        this.awayScore = awayScore == null? 0L : awayScore;
        this.homeSeed = homeSeed;
        this.awaySeed = awaySeed;
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
        this.homeSet = homeSet;
        this.awaySet = awaySet;
        this.periodSet = periodSet;
        this.startTimestamp = startTimestamp;
        this.winner = winner;
        this.status = status;
    }

    public void updateFrom(Match match){
        this.homeScore = match.getHomeScore();
        this.awayScore = match.getAwayScore();
        this.homeSet = match.getHomeSet();
        this.awaySet = match.getAwaySet();
        this.periodSet = match.getPeriodSet();
        this.startTimestamp = match.getStartTimestamp();
        this.winner = match.getWinner();
        this.status = match.getStatus();
    }

    public void nullToZero(){
        homeSet.nullToZero();
        awaySet.nullToZero();
    }

    public void convertTime() {
        if (periodSet != null) {
            periodSet.convertPeriods();
        }
        startTimestamp = ConversionUtil.timestampToYyyyMMddHHMMSS(startTimestamp);
    }

    public Season getSeason(){
        return round != null ? round.getSeason() : null;
    }

    public Tournament getTournament(){
        return round != null && round.getSeason() != null
                ? round.getSeason().getTournament() : null;
    }

    public Category getCategory(){
        return round != null && round.getSeason() != null && round.getSeason().getTournament() != null
                ? getTournament().getCategory() : null;

    }

    public void updatePlayer(Player homePlayer, Player awayPlayer){
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
    }

    public void updateRound(Round round){
        this.round = round;
    }

    // 진행했든, 안했든 종료
    public boolean isEnded(){
        return "Ended".equals(status) || "Retired".equals(status) || "Canceled".equals(status);
    }

    // 경기 진행 후 종료
    public boolean isFinished(){return "Ended".equals(status) || "Retired".equals(status);}

}
