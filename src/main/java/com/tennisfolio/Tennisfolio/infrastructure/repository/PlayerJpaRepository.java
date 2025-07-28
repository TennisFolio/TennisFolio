package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PlayerJpaRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByRapidPlayerId(String rapidPlayerId);
}
