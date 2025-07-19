package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface TournamentJpaRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT t FROM Tournament t WHERE t.rapidTournamentId IN :ids")
    List<Tournament> findByRapidTournamentIds(@Param("ids") List<String> ids);

    Optional<Tournament> findByRapidTournamentId(@Param("rapidId") String rapidId);
}
