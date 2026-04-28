package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameEntryRepository extends JpaRepository<GameEntry, Long> {

    List<GameEntry> findByGameId(Long gameId);

    List<GameEntry> findByCompetitionEntryId(Long competitionEntryId);

    @Query("SELECT ge FROM GameEntry ge WHERE ge.game.id = :gameId AND ge.team = :team")
    List<GameEntry> findByGameIdAndTeam(@Param("gameId") Long gameId,
                                        @Param("team") GameEntry.Team team);

    @Query("SELECT ge FROM GameEntry ge WHERE ge.competitionEntry.id = :competitionEntryId ORDER BY ge.game.round ASC")
    List<GameEntry> findByCompetitionEntryIdOrderByRound(@Param("competitionEntryId") Long competitionEntryId);

    long countByCompetitionEntryId(Long competitionEntryId);
}

