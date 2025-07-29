package com.tennisfolio.Tennisfolio.ranking.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RankingRepository {

    RankingEntity getById(Long id);

    RankingEntity save(RankingEntity rankingEntity);

    List<RankingEntity> saveAll(List<RankingEntity> rankingEntities);

    List<RankingEntity> findLatestRankings();

    List<RankingEntity> findLatestRankings(Pageable pageable);

    List<RankingEntity> findByLastUpdate(String lastUpdate);

    List<RankingEntity> collect(RankingEntity rankingEntity);

    List<RankingEntity> collect(List<RankingEntity> rankingEntity);

    boolean flushWhenFull();

    boolean flushAll();
}
