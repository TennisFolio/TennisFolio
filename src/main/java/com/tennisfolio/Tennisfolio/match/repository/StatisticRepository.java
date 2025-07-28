package com.tennisfolio.Tennisfolio.match.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatisticRepository extends JpaRepository<StatisticEntity, Long> {
    Optional<StatisticEntity> findByMatchEntityAndPeriodAndGroupName(MatchEntity matchEntity, String period, String group);
}
