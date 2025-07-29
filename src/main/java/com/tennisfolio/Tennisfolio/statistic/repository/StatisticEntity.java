package com.tennisfolio.Tennisfolio.statistic.repository;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_statistic")
@NoArgsConstructor
public class StatisticEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="STAT_ID")
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
                .build();
    }

}
