package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.match.domain.Match;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    Optional<Match> findByRapidMatchId(String rapidMatchId);
    List<Match> findAll();
}
