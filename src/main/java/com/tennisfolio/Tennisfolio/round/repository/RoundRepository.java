package com.tennisfolio.Tennisfolio.round.repository;

import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {
    Optional<RoundEntity> findBySeasonEntityAndRoundAndSlug(SeasonEntity seasonEntity, Long round, String slug);
}
