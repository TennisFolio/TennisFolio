package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RankingRepository {

    Ranking getById(Long id);

    Ranking save(Ranking ranking);

    List<Ranking> saveAll(List<Ranking> rankings);

    List<Ranking> findLatestRankings(Pageable pageable);

    List<Ranking> findByLastUpdate(String lastUpdate);

    List<Ranking> collect(Ranking ranking);

    List<Ranking> collect(List<Ranking> rankings);

    boolean flushWhenFull();

    boolean flushAll();
}
