package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByRapidSeasonId(String rapidId);
}
