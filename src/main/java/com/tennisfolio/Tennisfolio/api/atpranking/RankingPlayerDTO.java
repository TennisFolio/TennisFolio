package com.tennisfolio.Tennisfolio.api.atpranking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingPlayerDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String playerRapidId;
    @JsonProperty("slug")
    private String slug;
}
