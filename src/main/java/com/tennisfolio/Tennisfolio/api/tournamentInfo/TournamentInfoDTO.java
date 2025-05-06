package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentInfoDTO {
    @JsonProperty("cityName")
    private String city;
    @JsonProperty("matchType")
    private String matchType;
    @JsonProperty("surfaceType")
    private String groundType;
    @JsonProperty("uniqueTournament")
    private UniqueTournamentDTO tournament;

}
