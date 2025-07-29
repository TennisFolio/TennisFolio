package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface RoundJpaRepository extends JpaRepository<RoundEntity, Long> {
    Optional<RoundEntity> findBySeasonEntityAndRoundAndSlug(SeasonEntity seasonEntity, Long round, String slug);
}
