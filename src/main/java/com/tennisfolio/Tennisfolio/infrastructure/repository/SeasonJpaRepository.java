package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonJpaRepository extends JpaRepository<SeasonEntity, Long> {
    Optional<SeasonEntity> findByRapidSeasonId(String rapidId);
}
