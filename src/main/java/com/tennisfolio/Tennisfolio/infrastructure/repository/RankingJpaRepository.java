package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.player.repository.CountryEntity;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingQueryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface RankingJpaRepository extends JpaRepository<RankingEntity, Long>, RankingQueryRepository {

    @Query("SELECT r FROM RankingEntity r " +
            "JOIN FETCH r.playerEntity p " +
            "WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM RankingEntity r2) ORDER BY r.curRank")
    List<RankingEntity> findLatestRankings(Pageable pageable);

    @Query("SELECT r FROM RankingEntity r JOIN r.playerEntity p WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM RankingEntity r2) ORDER BY r.curRank")
    List<RankingEntity> findLatestRankingsBefore(Pageable pageable);

    List<RankingEntity> findByLastUpdate(String lastUpdate);

    boolean existsByLastUpdateAndCategory(String lastUpdate, RankingCategory category);

    @Query("""
            SELECT DISTINCT new com.tennisfolio.Tennisfolio.player.repository.CountryEntity(p.countryEntity.countryCode, p.countryEntity.countryName)
            FROM RankingEntity r
            JOIN r.playerEntity p
            WHERE r.lastUpdate = (SELECT MAX(r2.lastUpdate) FROM RankingEntity r2)
            ORDER BY p.countryEntity.countryName
            """)
    List<CountryEntity> findDistinctCountriesFromTopRankings();
}
