package com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.util.ConversionUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeDTO {
    @JsonProperty("period1")
    private String period1;
    @JsonProperty("period2")
    private String period2;
    @JsonProperty("period3")
    private String period3;
    @JsonProperty("period4")
    private String period4;
    @JsonProperty("period5")
    private String period5;
    @JsonProperty("currentPeriodStartTimestamp")
    private String currentPeriodStartTimestamp;

}
