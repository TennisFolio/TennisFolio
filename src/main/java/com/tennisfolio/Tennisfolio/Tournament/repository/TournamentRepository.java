package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TournamentRepository {
    Tournament save(Tournament tournament);

    List<Tournament> saveAll(List<Tournament> tournaments);

    List<Tournament> findAll();

    Set<String> findAllRapidIds();

    List<Tournament> findByRapidTournamentIds(List<String> ids);

    Optional<Tournament> findByRapidTournamentId(String rapidId);

    List<Tournament> collect(Tournament tournament);

    List<Tournament> collect(List<Tournament> tournaments);

    boolean flushWhenFull();

    boolean flushAll();
}
