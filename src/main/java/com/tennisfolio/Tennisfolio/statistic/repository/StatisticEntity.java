package com.tennisfolio.Tennisfolio.statistic.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_statistic")
@NoArgsConstructor
public class StatisticEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator = "statistic_gen")
    @TableGenerator(
            name = "statistic_gen",
            table= "TB_SEQUENCES",
            pkColumnName= "TABLE_ID",
            valueColumnName= "NEXT_VAL",
            pkColumnValue = "STAT_ID",
            allocationSize = 1000
    )
    private Long statId;
    @ManyToOne
    @JoinColumn(name="MATCH_ID")
    private MatchEntity matchEntity;
    @Column(name="PERIOD")
    private String period;
    @Column(name="GROUP_NAME")
    private String groupName;
    @Column(name="STAT_DIRECTION")
    private String statDirection;
    @Column(name="METRIC")
    private String metric;
    @Column(name="HOME_VALUE")
    private Long homeValue;
    @Column(name="AWAY_VALUE")
    private Long awayValue;
    @Column(name="HOME_TOTAL")
    private Long homeTotal;
    @Column(name="AWAY_TOTAL")
    private Long awayTotal;


    public static StatisticEntity fromModel(Statistic statistic) {
        StatisticEntity statisticEntity = new StatisticEntity();
        statisticEntity.statId = statistic.getStatId();
        statisticEntity.matchEntity = MatchEntity.fromModel(statistic.getMatch());
        statisticEntity.period = statistic.getPeriod();
        statisticEntity.groupName = statistic.getGroupName();
        statisticEntity.statDirection = statistic.getStatDirection();
        statisticEntity.metric = statistic.getMetric();
        statisticEntity.homeValue = statistic.getHomeValue();
        statisticEntity.awayValue = statistic.getAwayValue();
        statisticEntity.homeTotal = statistic.getHomeTotal();
        statisticEntity.awayTotal = statistic.getAwayTotal();
        return statisticEntity;
    }

    public Statistic toModel(){
        return Statistic.builder()
                .statId(statId)
                .match(matchEntity.toModel())
                .period(period)
                .statDirection(statDirection)
                .metric(metric)
                .homeValue(homeValue)
                .awayValue(awayValue)
                .homeTotal(homeTotal)
                .awayTotal(awayTotal)
                .build();
    }

}
