package com.tennisfolio.Tennisfolio.season.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {
    Optional<SeasonEntity> findByRapidSeasonId(String rapidId);
}
