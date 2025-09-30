package com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("statisticsType")
    private String statisticsType;
    @JsonProperty("valueType")
    private String valueType;
    @JsonProperty("homeValue")
    private Long homeValue;
    @JsonProperty("homeTotal")
    private Long homeTotal;
    @JsonProperty("awayValue")
    private Long awayValue;
    @JsonProperty("awayTotal")
    private Long awayTotal;
    @JsonProperty("key")
    private String metric;
}
