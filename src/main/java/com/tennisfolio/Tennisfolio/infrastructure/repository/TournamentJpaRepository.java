package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, Long> {
    @Query("SELECT t FROM TournamentEntity t WHERE t.rapidTournamentId IN :ids")
    List<TournamentEntity> findByRapidTournamentIds(@Param("ids") List<String> ids);

    Optional<TournamentEntity> findByRapidTournamentId(@Param("rapidId") String rapidTournamentId);

    @Query("SELECT t.rapidTournamentId FROM TournamentEntity t")
    Set<String> findAllRapidTournamentIds();
}
