package com.tennisfolio.Tennisfolio.infrastructure.repository;

import ch.qos.logback.core.joran.sanity.Pair;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;


public interface RoundJpaRepository extends JpaRepository<RoundEntity, Long> {
    Optional<RoundEntity> findBySeasonEntityAndRoundAndSlug(SeasonEntity seasonEntity, Long round, String slug);
    @Query("SELECT r.seasonEntity, r.round FROM RoundEntity r")
    Set<Pair<SeasonEntity,String>> findAllSeasonRoundPairs();
}
