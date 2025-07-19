package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    Player getById(Long id);

    Optional<Player> findByRapidPlayerId(String rapidPlayerId);

    Player save(Player player);

    List<Player> saveAll(List<Player> players);

    List<Player> bufferedSave(Player player);

    List<Player> bufferedSaveAll(List<Player> players);

    List<Player> flush();

}
