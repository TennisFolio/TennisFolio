package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_match")
@NoArgsConstructor
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MATCH_ID")
    private Long matchId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROUND_ID")
    private RoundEntity roundEntity;
    @Column(name="RAPID_MATCH_ID")
    private String rapidMatchId;
    @Column(name="HOME_SEED")
    private String homeSeed;
    @Column(name="AWAY_SEED")
    private String awaySeed;
    @Column(name="HOME_SCORE")
    private Long homeScore;
    @Column(name="AWAY_SCORE")
    private Long awayScore;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="HOME_PLAYER", nullable = true)
    private PlayerEntity homePlayer;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AWAY_PLAYER", nullable = true)
    private PlayerEntity awayPlayer;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name= "set1", column = @Column(name="HOME_SET1")),
            @AttributeOverride(name= "set2", column = @Column(name="HOME_SET2")),
            @AttributeOverride(name= "set3", column = @Column(name="HOME_SET3")),
            @AttributeOverride(name= "set4", column = @Column(name="HOME_SET4")),
            @AttributeOverride(name= "set5", column = @Column(name="HOME_SET5")),
            @AttributeOverride(name= "set1Tie", column = @Column(name="HOME_TIE_SET1")),
            @AttributeOverride(name= "set2Tie", column = @Column(name="HOME_TIE_SET2")),
            @AttributeOverride(name= "set3Tie", column = @Column(name="HOME_TIE_SET3")),
            @AttributeOverride(name= "set4Tie", column = @Column(name="HOME_TIE_SET4")),
            @AttributeOverride(name= "set5Tie", column = @Column(name="HOME_TIE_SET5"))
    })
    private Score homeSet;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name= "set1", column = @Column(name="AWAY_SET1")),
            @AttributeOverride(name= "set2", column = @Column(name="AWAY_SET2")),
            @AttributeOverride(name= "set3", column = @Column(name="AWAY_SET3")),
            @AttributeOverride(name= "set4", column = @Column(name="AWAY_SET4")),
            @AttributeOverride(name= "set5", column = @Column(name="AWAY_SET5")),
            @AttributeOverride(name= "set1Tie", column = @Column(name="AWAY_TIE_SET1")),
            @AttributeOverride(name= "set2Tie", column = @Column(name="AWAY_TIE_SET2")),
            @AttributeOverride(name= "set3Tie", column = @Column(name="AWAY_TIE_SET3")),
            @AttributeOverride(name= "set4Tie", column = @Column(name="AWAY_TIE_SET4")),
            @AttributeOverride(name= "set5Tie", column = @Column(name="AWAY_TIE_SET5"))
    })
    private Score awaySet;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name= "set1", column = @Column(name="PERIOD_SET1")),
            @AttributeOverride(name= "set2", column = @Column(name="PERIOD_SET2")),
            @AttributeOverride(name= "set3", column = @Column(name="PERIOD_SET3")),
            @AttributeOverride(name= "set4", column = @Column(name="PERIOD_SET4")),
            @AttributeOverride(name= "set5", column = @Column(name="PERIOD_SET5"))
    })
    private Period periodSet;

    @Column(name="START_TIMESTAMP")
    private String startTimeStamp;

    @Column(name="WINNER")
    private String winner;

    public static MatchEntity fromModel(Match match) {
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.matchId = match.getMatchId();
        matchEntity.roundEntity = RoundEntity.fromModel(match.getRound());
        matchEntity.rapidMatchId = match.getRapidMatchId();
        matchEntity.homeSeed = match.getHomeSeed();
        matchEntity.awaySeed = match.getAwaySeed();
        matchEntity.homeScore = match.getHomeScore();
        matchEntity.awayScore = match.getAwayScore();
        matchEntity.homePlayer = PlayerEntity.fromModel(match.getHomePlayer());
        matchEntity.awayPlayer = PlayerEntity.fromModel(match.getAwayPlayer());
        matchEntity.homeSet = match.getHomeSet();
        matchEntity.awaySet = match.getAwaySet();
        matchEntity.periodSet = match.getPeriodSet();
        matchEntity.startTimeStamp = match.getStartTimeStamp();
        matchEntity.winner = match.getWinner();

        return matchEntity;
    }

    public Match toModel(){
        return Match.builder()
                .matchId(matchId)
                .round(roundEntity.toModel())
                .rapidMatchId(rapidMatchId)
                .homeSeed(homeSeed)
                .awaySeed(awaySeed)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .homePlayer(homePlayer.toModel())
                .awayPlayer(awayPlayer.toModel())
                .homeSet(homeSet)
                .awaySet(awaySet)
                .periodSet(periodSet)
                .startTimeStamp(startTimeStamp)
                .winner(winner)
                .build();
    }
}
