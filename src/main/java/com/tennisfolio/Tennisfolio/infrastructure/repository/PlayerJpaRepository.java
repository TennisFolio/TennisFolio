package com.tennisfolio.Tennisfolio.infrastructure.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface PlayerJpaRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByRapidPlayerId(String rapidPlayerId);
}
