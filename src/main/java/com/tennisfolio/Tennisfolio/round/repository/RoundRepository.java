package com.tennisfolio.Tennisfolio.round.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {
    Optional<Round> findBySeasonAndRoundAndSlug(Season season, Long round, String slug);
}
