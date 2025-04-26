package com.tennisfolio.Tennisfolio.api.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.api.teamdetails.CountryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {
    @JsonProperty("id")
    private String rapidPlayerId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ranking")
    private String ranking;
    @JsonProperty("country")
    private CountryDTO country;

}
