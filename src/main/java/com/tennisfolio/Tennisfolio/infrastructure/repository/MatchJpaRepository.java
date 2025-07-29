package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByRapidMatchId(String rapidMatchId);
}
