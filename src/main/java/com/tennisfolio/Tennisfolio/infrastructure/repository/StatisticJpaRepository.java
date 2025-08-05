package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatisticJpaRepository extends JpaRepository<StatisticEntity, Long> {
    Optional<StatisticEntity> findByMatchEntityAndPeriodAndGroupName(MatchEntity matchEntity, String period, String group);
}
