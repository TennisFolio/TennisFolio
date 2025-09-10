package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TournamentRepository {
    Tournament save(Tournament tournament);

    List<Tournament> saveAll(List<Tournament> tournaments);

    List<Tournament> findAll();

    List<Tournament> findAllWithPlayers();

    Set<String> findAllRapidIds();

    List<Tournament> findByRapidTournamentIds(List<String> ids);
    List<Tournament> findByCategoryIn(List<Category> ids);
    Optional<Tournament> findByRapidTournamentId(String rapidId);
    Optional<Tournament> findWithCategoryByRapidTournamentId(String rapidId);

    List<Tournament> collect(Tournament tournament);

    List<Tournament> collect(List<Tournament> tournaments);

    boolean flushWhenFull();

    boolean flushAll();
}
