package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.api.teamdetails.CountryDTO;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveEventsApiDTO {
    @JsonProperty("id")
    private String rapidId;
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
    @JsonProperty("time")
    private TimeDTO time;
    @JsonProperty("startTimestamp")
    private String startTime;

    public void convertTime() {
        if (time != null) {
            time.convertPeriods();
            time.convertCurrentPeriodStartTimestamp();
        }
        this.startTime = ConversionUtil.timestampToYyyyMMdd(this.startTime);
    }

    public void scoreNullToZero() {
        homeScore.nullToZero();
        awayScore.nullToZero();
    }

}
