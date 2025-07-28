package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics.StatisticsDTO;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Statistic {

    private Long statId;

    private Match match;

    private String period;

    private String groupName;

    private String statDirection;

    private String metric;

    private Long homeValue;

    private Long awayValue;

    public Statistic(String period, StatisticsDTO dto, Match match){
        this.match = match;
        this.period = period;
        this.groupName = dto.getName();
        this.statDirection = dto.getStatisticsType();
        this.metric = dto.getName();
        this.homeValue = dto.getHomeValue();
        this.awayValue = dto.getAwayValue();

    }
}
