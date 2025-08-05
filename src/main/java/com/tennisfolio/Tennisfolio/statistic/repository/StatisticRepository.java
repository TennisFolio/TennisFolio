package com.tennisfolio.Tennisfolio.statistic.repository;


import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatisticRepository {
    List<Statistic> saveAll(List<Statistic> statistics);
}