package com.tennisfolio.Tennisfolio.matching.repository;

import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetitionStatRepository extends JpaRepository<CompetitionStat, Long> {

    Optional<CompetitionStat> findByCompetitionId(Long competitionId);
}

