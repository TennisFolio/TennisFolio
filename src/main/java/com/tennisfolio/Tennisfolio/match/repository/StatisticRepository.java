package com.tennisfolio.Tennisfolio.match.repository;


import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    Optional<Statistic> findByMatchAndPeriodAndGroupName(Match match, String period, String group);
}
