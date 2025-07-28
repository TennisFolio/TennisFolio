package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import jakarta.persistence.*;
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
