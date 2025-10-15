package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueDetailsDTO {
    @JsonProperty("id")
    private String rapidId;
    @JsonProperty("titleHolder")
    private LeagueDetailsPlayerDTO titleHolder;
    @JsonProperty("mostTitles")
    private String mostTitles;
    @JsonProperty("mostTitlesTeams")
    private List<LeagueDetailsPlayerDTO> mostTitlesTeams;
    @JsonProperty("tennisPoints")
    private Long points;
    @JsonProperty("startDateTimestamp")
    private String startTimestamp;
    @JsonProperty("endDateTimestamp")
    private String endTimestamp;

    public Optional<String> getMostTitlePlayerRapidId(){
        return Optional.ofNullable(mostTitlesTeams)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getRapidId());
    }

}
