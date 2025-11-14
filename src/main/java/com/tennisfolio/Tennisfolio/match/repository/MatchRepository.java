package com.tennisfolio.Tennisfolio.match.repository;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.round.domain.Round;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchRepository {
    Optional<Match> findByRapidMatchId(String rapidMatchId);
    Match save(Match match);
    void saveAll(List<Match> matches);
    List<Match> findAll();
    Set<String> findAllRapidIds();
    List<Match> collect(Match match);
    List<Match> collect(List<Match> matches);
    boolean flushWhenFull();
    boolean flushAll();
    void flush();
    void updateMatch(Match match);
}
