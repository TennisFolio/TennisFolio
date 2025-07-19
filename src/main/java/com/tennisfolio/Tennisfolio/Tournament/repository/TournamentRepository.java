package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {
    Tournament save(Tournament tournament);

    List<Tournament> saveAll(List<Tournament> tournaments);

    List<Tournament> findByRapidTournamentIds(List<String> ids);

    Optional<Tournament> findByRapidTournamentId(String rapidId);

    List<Tournament> bufferedSave(Tournament tournament);

    List<Tournament> bufferedSaveAll(List<Tournament> tournaments);

    List<Tournament> flush();
}
