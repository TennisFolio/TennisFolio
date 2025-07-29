package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.player.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    Player getById(Long id);

    Player findByRapidPlayerId(String rapidPlayerId);

    boolean existsByRapidPlayerId(String rapidPlayerId);

    Player save(Player player);

    List<Player> saveAll(List<Player> players);


}

