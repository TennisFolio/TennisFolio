package com.tennisfolio.Tennisfolio.season.service;

import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.List;

public interface SeasonService {
    List<Season> saveSeasonList();
    List<Season> saveSeasonInfo();
}
