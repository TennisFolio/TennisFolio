package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository  extends JpaRepository<Match, Long> {
    Optional<Match> findByRapidMatchId(String rapidMatchId);
}
