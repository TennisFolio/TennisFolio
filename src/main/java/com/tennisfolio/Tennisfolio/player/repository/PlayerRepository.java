package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByRapidPlayerId(String rapidPlayerId);
}
