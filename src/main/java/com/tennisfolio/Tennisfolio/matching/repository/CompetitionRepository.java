package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    Optional<Competition> findByName(String name);

    Optional<Competition> findByPublicId(String publicId);

    List<Competition> findByStatus(Competition.CompetitionStatus status);

    @Query("SELECT c FROM Competition c WHERE c.isModified = true")
    List<Competition> findModifiedCompetitions();
}

