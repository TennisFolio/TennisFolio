package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("statisticsType")
    private String statisticsType;
    @JsonProperty("homeValue")
    private Long homeValue;
    @JsonProperty("awayValue")
    private Long awayValue;
}
