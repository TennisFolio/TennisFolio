package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StatisticJpaRepository extends JpaRepository<StatisticEntity, Long> {
    Optional<StatisticEntity> findByMatchEntityAndPeriodAndGroupName(MatchEntity matchEntity, String period, String group);
    List<StatisticEntity> findByMatchEntity(MatchEntity matchEntity);
    @Query("""
            SELECT s
            FROM StatisticEntity s
            JOIN FETCH s.matchEntity m
            JOIN FETCH m.homePlayer hp
            JOIN FETCH m.awayPlayer ap
            WHERE m.startTimestamp LIKE CONCAT(:year, '%')
            """)
    List<StatisticEntity> findWithMatchAndPlayerByYear(@Param("year") String year);
}
