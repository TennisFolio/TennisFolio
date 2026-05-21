package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionEntryRepository extends JpaRepository<CompetitionEntry, Long> {

    List<CompetitionEntry> findByCompetitionId(Long competitionId);

    List<CompetitionEntry> findByCompetitionIdOrderByIdAsc(Long competitionId);

    List<CompetitionEntry> findByCompetitionIdAndStatus(Long competitionId, CompetitionEntry.EntryStatus status);

    Optional<CompetitionEntry> findByIdAndCompetitionId(Long id, Long competitionId);

    Optional<CompetitionEntry> findByPlayerNameAndCompetitionId(String playerName, Long competitionId);

    @Query("SELECT ce FROM CompetitionEntry ce WHERE ce.competition.id = :competitionId AND ce.gender = :gender")
    List<CompetitionEntry> findByCompetitionIdAndGender(@Param("competitionId") Long competitionId,
                                                        @Param("gender") CompetitionEntry.Gender gender);

    long countByCompetitionId(Long competitionId);
}

