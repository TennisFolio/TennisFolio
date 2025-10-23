package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface RankingJpaRepository extends JpaRepository<RankingEntity, Long> {

    @Query("SELECT r FROM RankingEntity r " +
            "JOIN FETCH r.playerEntity p " +
            "JOIN FETCH p.prizeEntity pp " +
            "WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM RankingEntity r2) ORDER BY r.curRank")
    List<RankingEntity> findLatestRankings(Pageable pageable);

    @Query("SELECT r FROM RankingEntity r JOIN r.playerEntity p WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM RankingEntity r2) ORDER BY r.curRank")
    List<RankingEntity> findLatestRankingsBefore(Pageable pageable);

    List<RankingEntity> findByLastUpdate(String lastUpdate);

    boolean existsByLastUpdate(String lastUpdate);
}
