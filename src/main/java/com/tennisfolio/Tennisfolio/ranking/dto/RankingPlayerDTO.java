package com.tennisfolio.Tennisfolio.ranking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
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
