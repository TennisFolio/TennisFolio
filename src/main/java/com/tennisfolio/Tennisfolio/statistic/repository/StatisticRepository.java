package com.tennisfolio.Tennisfolio.statistic.repository;

import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;

import java.util.List;

public interface StatisticRepository {
    List<Statistic> saveAll(List<Statistic> statistics);
}
