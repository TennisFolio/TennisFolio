package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
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
    @JsonProperty("status")
    private StatusDTO status;
    @JsonProperty("time")
    private TimeDTO time;
    @JsonProperty("startTimestamp")
    private String startTime;


    public int getTotalRanking(){
        long home = parseRanking(homeTeam.getRanking());
        long away = parseRanking(awayTeam.getRanking());
        long sum = home + away;

        return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    private int parseRanking(String ranking){
        try {
            return ranking != null ? Integer.parseInt(ranking) : Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public boolean isAtpEvent() {
        return tournament != null
                && tournament.getCategory() != null
                && "atp".equalsIgnoreCase(tournament.getCategory().getSlug());
    }

    public boolean isWtaEvent() {
        return tournament != null
                && tournament.getCategory() != null
                && "wta".equalsIgnoreCase(tournament.getCategory().getSlug());
    }

}
