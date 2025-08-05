package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.player.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    Optional<Player> findById(Long id);

    Optional<Player> findByRapidPlayerId(String rapidPlayerId);

    boolean existsByRapidPlayerId(String rapidPlayerId);

    Player save(Player player);

    List<Player> saveAll(List<Player> players);


}

