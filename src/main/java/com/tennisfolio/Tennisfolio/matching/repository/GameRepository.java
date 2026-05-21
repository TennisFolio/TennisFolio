package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByCompetitionId(Long competitionId);

    Optional<Game> findByIdAndCompetitionId(Long id, Long competitionId);

    List<Game> findByCompetitionIdAndRound(Long competitionId, Integer round);

    List<Game> findByCompetitionIdAndStatus(Long competitionId, Game.GameStatus status);

    @Query("SELECT COALESCE(MAX(g.round), 0) FROM Game g WHERE g.competition.id = :competitionId AND g.court = :court")
    int findMaxRoundByCompetitionIdAndCourt(@Param("competitionId") Long competitionId, @Param("court") Integer court);

    @Query("SELECT g FROM Game g WHERE g.competition.id = :competitionId ORDER BY g.round ASC, g.court ASC")
    List<Game> findByCompetitionIdOrderByRoundAndCourt(@Param("competitionId") Long competitionId);

    long countByCompetitionId(Long competitionId);

    long countByCompetitionIdAndMatchType(Long competitionId, Game.MatchType matchType);
}

