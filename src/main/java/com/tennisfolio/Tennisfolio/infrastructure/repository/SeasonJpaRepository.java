package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface SeasonJpaRepository extends JpaRepository<SeasonEntity, Long> {
    Optional<SeasonEntity> findByRapidSeasonId(String rapidId);

    @Query("SELECT s.rapidSeasonId FROM SeasonEntity s")
    Set<String> findAllRapidSeasonIds();
}
