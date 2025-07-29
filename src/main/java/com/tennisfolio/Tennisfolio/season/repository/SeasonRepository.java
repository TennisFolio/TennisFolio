package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;

public interface SeasonRepository {
    List<Season> findAll();
}
