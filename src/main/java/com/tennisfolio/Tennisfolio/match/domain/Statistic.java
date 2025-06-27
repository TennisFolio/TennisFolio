package com.tennisfolio.Tennisfolio.match.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.match.eventStatistics.StatisticsDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_statistic")
@NoArgsConstructor
public class Statistic extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="STAT_ID")
    private Long statId;
    @ManyToOne
    @JoinColumn(name="MATCH_ID")
    private Match match;
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
