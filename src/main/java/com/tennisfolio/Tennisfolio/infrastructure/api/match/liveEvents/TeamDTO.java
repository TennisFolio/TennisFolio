package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.player.dto.CountryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
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
