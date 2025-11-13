package com.tennisfolio.Tennisfolio.infrastructure.api.match.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO {
    @JsonProperty("id")
    private String rapidId;
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
    @JsonProperty("homeTeam")
    private TeamDTO homeTeam;
    @JsonProperty("awayTeam")
    private TeamDTO awayTeam;
    @JsonProperty("homeScore")
    private ScoreDTO homeScore;
    @JsonProperty("awayScore")
    private ScoreDTO awayScore;
    @JsonProperty("status")
    private StatusDTO status;
    @JsonProperty("time")
    private TimeDTO time;
    @JsonProperty("startTimestamp")
    private String startTimestamp;
    @JsonProperty("winnerCode")
    private String winner;
}
