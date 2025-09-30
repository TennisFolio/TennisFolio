package com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsGroupDTO {
    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("statisticsItems")
    private List<StatisticsDTO> statisticsList;
}
