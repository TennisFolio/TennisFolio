package com.tennisfolio.Tennisfolio.api.leagueDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueDetailsPlayerDTO {
    @JsonProperty("id")
    private String rapidId;
    @JsonProperty("name")
    private String name;

}
