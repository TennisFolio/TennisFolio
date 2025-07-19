package com.tennisfolio.Tennisfolio.ranking.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RankingRepository {

    Ranking getById(Long id);

    Ranking save(Ranking ranking);

    List<Ranking> saveAll(List<Ranking> rankings);

    List<Ranking> bufferedSave(Ranking ranking);
    List<Ranking> bufferedSaveAll(List<Ranking> rankings);
    List<Ranking> flush();

    List<Ranking> findLatestRankings();

    List<Ranking> findLatestRankings(Pageable pageable);

    List<Ranking> findByLastUpdate(String lastUpdate);
}
