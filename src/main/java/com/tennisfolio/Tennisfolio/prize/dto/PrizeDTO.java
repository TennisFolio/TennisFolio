package com.tennisfolio.Tennisfolio.prize.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrizeDTO {
    @JsonProperty("value")
    private Long value;
    @JsonProperty("currency")
    private String currency;
}
