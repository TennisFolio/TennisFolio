package com.tennisfolio.Tennisfolio.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository  extends JpaRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByRapidMatchId(String rapidMatchId);
}
