package com.tennisfolio.Tennisfolio.api.leagueSeasons;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueSeasonsDTO {
    @JsonProperty("id")
    private String seasonRapidId;
    @JsonProperty("name")
    private String seasonName;
    @JsonProperty("year")
    private String year;

}
