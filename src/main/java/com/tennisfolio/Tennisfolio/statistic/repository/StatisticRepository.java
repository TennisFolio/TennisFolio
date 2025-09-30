package com.tennisfolio.Tennisfolio.statistic.repository;


import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatisticRepository {

    List<Statistic> findAll();
    List<Statistic> findByMatch(Match match);
    List<Statistic> collect(Statistic statistic);
    List<Statistic> collect(List<Statistic> statistics);
    boolean flushWhenFull();

    boolean flushAll();
}