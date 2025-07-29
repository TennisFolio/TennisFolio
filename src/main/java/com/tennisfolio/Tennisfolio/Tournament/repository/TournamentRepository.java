package com.tennisfolio.Tennisfolio.Tournament.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TournamentRepository {
    TournamentEntity save(TournamentEntity tournament);

    List<TournamentEntity> saveAll(List<TournamentEntity> tournaments);

    List<TournamentEntity> findAll();

    Set<String> findAllRapidIds();

    List<TournamentEntity> findByRapidTournamentIds(List<String> ids);

    Optional<TournamentEntity> findByRapidTournamentId(String rapidId);

    List<TournamentEntity> collect(TournamentEntity tournament);

    List<TournamentEntity> collect(List<TournamentEntity> tournaments);

    boolean flushWhenFull();

    boolean flushAll();
}
