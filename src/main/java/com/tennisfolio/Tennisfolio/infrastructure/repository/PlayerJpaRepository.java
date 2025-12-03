package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface PlayerJpaRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByRapidPlayerId(String rapidPlayerId);
    boolean existsByRapidPlayerId(String rapidPlayerId);
    @Query("""
            SELECT p FROM PlayerEntity p WHERE p.playerName NOT LIKE '%/%'
            """)
    List<PlayerEntity> findSinglePlayer();
}
