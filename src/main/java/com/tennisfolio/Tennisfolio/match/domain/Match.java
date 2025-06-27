package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_match")
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MATCH_ID")
    private Long matchId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROUND_ID")
    private Round round;
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
    private Player homePlayer;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="AWAY_PLAYER", nullable = true)
    private Player awayPlayer;
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

    public Match(LeagueEventsByRoundDTO dto, Round round, Player homePlayer, Player awayPlayer){
        this.round = round;
        this.rapidMatchId = dto.getRapidMatchId();
        this.homeScore = dto.getHomeScore().getCurrent();
        this.awayScore = dto.getAwayScore().getCurrent();
        this.homeSeed = dto.getHomeTeamSeed();
        this.awaySeed = dto.getAwayTeamSeed();
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
        this.homeSet = new Score(dto.getHomeScore());
        this.awaySet = new Score(dto.getAwayScore());
        this.periodSet = new Period(dto.getTime());
        this.startTimeStamp = dto.getStartTimestamp();
        this.winner = dto.getWinner();


    }
}
