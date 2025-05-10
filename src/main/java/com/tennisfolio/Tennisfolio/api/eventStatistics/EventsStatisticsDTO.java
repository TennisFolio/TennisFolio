package com.tennisfolio.Tennisfolio.api.eventStatistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventsStatisticsDTO {
    @JsonProperty("period")
    private String period;
    @JsonProperty("groups")
    private List<StatisticsGroupDTO> groups;
}
