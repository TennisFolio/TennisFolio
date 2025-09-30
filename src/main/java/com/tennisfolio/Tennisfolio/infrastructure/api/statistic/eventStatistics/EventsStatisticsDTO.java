package com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventsStatisticsDTO {
    @JsonProperty("period")
    private String period;
    @JsonProperty("groups")
    private List<StatisticsGroupDTO> groups;
}
