package com.tennisfolio.Tennisfolio.round.repository;

import org.apache.commons.lang3.tuple.Pair;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoundRepository {
    Round save(Round round);
    List<Round> findAll();
    Set<Pair<Season, String>> findAllSeasonRoundPairs();
    Optional<Round> findBySeasonAndRoundAndSlug(Season season, Long round, String slug);
    Optional<Round> findBySeasonAndRound(Season season, Long round);
    List<Round> collect(Round round);
    List<Round> collect(List<Round> rounds);
    List<Round> findBySeasonAndRoundIn(Season season, Set<Long> rounds);
    boolean flushWhenFull();
    boolean flushAll();
    void flush();
}
