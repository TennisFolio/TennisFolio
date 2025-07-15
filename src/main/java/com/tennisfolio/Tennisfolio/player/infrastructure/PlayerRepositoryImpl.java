package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.player.application.port.PlayerRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;

import java.util.Optional;

public class PlayerRepositoryImpl implements PlayerRepository {
    private final PlayerJpaRepository playerJpaRepository;

    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository) {
        this.playerJpaRepository = playerJpaRepository;
    }

    @Override
    public Player save(Player player) {
        return playerJpaRepository.save(player);
    }

    @Override
    public Optional<Player> findByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.findByRapidPlayerId(rapidPlayerId);
    }
}
