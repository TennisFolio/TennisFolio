package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.*;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueEventsByRoundDTO {
    @JsonProperty("id")
    private String rapidMatchId;
    @JsonProperty("homeTeamSeed")
    private String homeTeamSeed;
    @JsonProperty("awayTeamSeed")
    private String awayTeamSeed;
    @JsonProperty("tournament")
    private TournamentDTO tournament;
    @JsonProperty("season")
    private SeasonDTO season;
    @JsonProperty("roundInfo")
    private RoundDTO round;
    @JsonProperty("winnerCode")
    private String winner;
    @JsonProperty("homeTeam")
    private TeamDTO homeTeam;
    @JsonProperty("awayTeam")
    private TeamDTO awayTeam;
    @JsonProperty("homeScore")
    private ScoreDTO homeScore;
    @JsonProperty("awayScore")
    private ScoreDTO awayScore;
    @JsonProperty("time")
    private TimeDTO time;
    @JsonProperty("startTimestamp")
    private String startTimestamp;

    public void nullToZero(){
        homeScore.nullToZero();
        awayScore.nullToZero();
    }

    public void convertTime() {
        if (time != null) {
            time.convertPeriods();
            time.convertCurrentPeriodStartTimestamp();
        }
        this.startTimestamp = ConversionUtil.timestampToYyyyMMdd(this.startTimestamp);
    }
}
