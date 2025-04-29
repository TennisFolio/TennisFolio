package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    @Query("SELECT r FROM Ranking r JOIN r.player p WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM Ranking r2) ORDER BY r.curRank")
    List<Ranking> findLatestRankings();

    @Query("SELECT r FROM Ranking r JOIN r.player p WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM Ranking r2) ORDER BY r.curRank")
    List<Ranking> findLatestRankings(Pageable pageable);
}
