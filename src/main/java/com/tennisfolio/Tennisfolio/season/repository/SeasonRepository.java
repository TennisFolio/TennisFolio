package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeasonRepository {
    List<Season> findAll();
    Season save(Season season);
    List<Season> collect(Season season);
    List<Season> collect(List<Season> seasons);
    Set<String> findAllRapidIds();
    List<Season> findByRapidSeasonIdIn(Set<String> rapidSeasonIds);
    Optional<Season> findByRapidSeasonId(String rapidId);
    boolean flushWhenFull();
    boolean flushAll();
    void flush();
}
