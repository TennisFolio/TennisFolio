package com.tennisfolio.Tennisfolio.infrastructure.repository;

import ch.qos.logback.core.joran.sanity.Pair;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface RoundJpaRepository extends JpaRepository<RoundEntity, Long> {
    Optional<RoundEntity> findBySeasonEntityAndRoundAndSlug(SeasonEntity seasonEntity, Long round, String slug);
    Optional<RoundEntity> findBySeasonEntityAndRound(SeasonEntity seasonEntity, Long round);
    @Query("SELECT r.seasonEntity, r.round FROM RoundEntity r")
    Set<Pair<SeasonEntity,String>> findAllSeasonRoundPairs();

    @Query("""
            SELECT r
            FROM RoundEntity r
            JOIN FETCH r.seasonEntity s
            JOIN FETCH s.tournamentEntity t
            JOIN FETCH t.categoryEntity c
            """)
    List<RoundEntity> findAll();

    @Query("""
            SELECT r
            FROM RoundEntity r
            JOIN FETCH r.seasonEntity s
            JOIN FETCH s.tournamentEntity t
            JOIN FETCH t.categoryEntity c
            WHERE r.seasonEntity = :seasonEntity
            AND r.round IN :rounds 
            """)
    List<RoundEntity> findBySeasonAndRoundIn(@Param("seasonEntity") SeasonEntity season, @Param("rounds") Collection<Long> rounds);
}
