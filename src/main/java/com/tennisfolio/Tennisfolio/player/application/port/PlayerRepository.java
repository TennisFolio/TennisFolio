package com.tennisfolio.Tennisfolio.player.application.port;

import com.tennisfolio.Tennisfolio.player.domain.Player;

import java.util.Optional;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findByRapidPlayerId(String rapidPlayerId);
}
