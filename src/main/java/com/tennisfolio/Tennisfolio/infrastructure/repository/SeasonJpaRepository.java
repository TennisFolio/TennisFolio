package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeasonJpaRepository extends JpaRepository<SeasonEntity, Long> {
    Optional<SeasonEntity> findByRapidSeasonId(String rapidId);

    @Query("SELECT s.rapidSeasonId FROM SeasonEntity s")
    Set<String> findAllRapidSeasonIds();


    @Query("""
            SELECT s 
            FROM SeasonEntity s
            JOIN FETCH s.tournamentEntity t
            JOIN FETCH t.categoryEntity c 
            WHERE s.rapidSeasonId IN :ids
            """)
    List<SeasonEntity> findByRapidSeasonIdIn(@Param("ids") Collection<String> ids);

}
