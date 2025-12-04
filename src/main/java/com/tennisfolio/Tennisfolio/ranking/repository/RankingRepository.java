package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.common.RankingSearchCondition;
import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RankingRepository {

    Optional<Ranking> getById(Long id);

    Ranking save(Ranking ranking);

    List<Ranking> saveAll(List<Ranking> rankings);

    List<Ranking> findLatestRankings(Pageable pageable);

    List<Ranking> findLatestRankingsBefore(Pageable pageable);

    List<Ranking> findByLastUpdate(String lastUpdate);

    boolean existsByLastUpdate(String lastUpdate);

    List<Ranking> collect(Ranking ranking);

    List<Ranking> collect(List<Ranking> rankings);

    boolean flushWhenFull();

    boolean flushAll();

    List<Country> getDistinctCountriesFromTopRankings();

    Page<Ranking> search(Pageable pageable, RankingSearchCondition condition, String keyword);
}
