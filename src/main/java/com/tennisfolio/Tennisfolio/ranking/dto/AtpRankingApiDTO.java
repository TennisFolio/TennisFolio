package com.tennisfolio.Tennisfolio.ranking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtpRankingApiDTO {
    @JsonProperty("team")
    private RankingPlayerDTO team;
    @JsonProperty("ranking")
    private Long curRank;
    @JsonProperty("previousRanking")
    private Long preRank;
    @JsonProperty("points")
    private Long point;
    @JsonProperty("previousPoints")
    private Long prePoints;
    @JsonProperty("bestRanking")
    private Long bestRank;
    @JsonProperty("updateAtTimestamp")
    private String updateTime;
}
