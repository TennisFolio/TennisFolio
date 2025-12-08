package com.tennisfolio.Tennisfolio.statistic.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.StatisticsDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
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

    private Long homeTotal;

    private Long awayTotal;

    public Statistic(String period, StatisticsDTO dto, Match match){
        this.match = match;
        this.period = period;
        this.groupName = dto.getName();
        this.statDirection = dto.getStatisticsType();
        this.metric = dto.getMetric();
        this.homeValue = dto.getHomeValue();
        this.awayValue = dto.getAwayValue();
        this.homeTotal = dto.getHomeTotal();
        this.awayTotal = dto.getAwayTotal();

    }

    public void updateMatch(Match match){
        this.match = match;
    }
}
