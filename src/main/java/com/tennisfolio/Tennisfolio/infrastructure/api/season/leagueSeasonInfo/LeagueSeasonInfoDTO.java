package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueSeasonInfoDTO {
    @JsonProperty("totalPrizeMoney")
    private Long totalPrizeMoney;
    @JsonProperty("totalPrizeMoneyCurrency")
    private String currency;
    @JsonProperty("numberOfCompetitors")
    private Long competitors;

}
