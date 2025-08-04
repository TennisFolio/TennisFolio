package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByRapidMatchId(String rapidMatchId);

    @Query("SELECT m.rapidMatchId FROM MatchEntity m")
    Set<String> findAllRapidMatchIds();
}
