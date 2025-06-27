package com.tennisfolio.Tennisfolio.player.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.prize.dto.PrizeDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDetailsApiDTO {

    @JsonProperty("id")
    private String playerRapidId;
    @JsonProperty("fullName")
    private String playerName;
    @JsonProperty("country")
    private CountryDTO country;
    @JsonProperty("height")
    private String height;
    @JsonProperty("weight")
    private String weight;
    @JsonProperty("plays")
    private String plays;
    @JsonProperty("turnedPro")
    private String turnedPro;
    @JsonProperty("prizeCurrentRaw")
    private PrizeDTO prizeCurrent;
    @JsonProperty("prizeTotalRaw")
    private PrizeDTO prizeTotal;
    @JsonProperty("birthDateTimestamp")
    private String birthDate;
}
