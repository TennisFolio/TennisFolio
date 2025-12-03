package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerJpaRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository{

    private final PlayerJpaRepository playerJpaRepository;

    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository) {
        this.playerJpaRepository = playerJpaRepository;
    }

    @Override
    public Optional<Player> findById(Long id) {
        return playerJpaRepository.findById(id).map(PlayerEntity::toModel);

    }

    @Override
    public Optional<Player> findByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.findByRapidPlayerId(rapidPlayerId).map(PlayerEntity::toModel);
    }

    @Override
    public boolean existsByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.existsByRapidPlayerId(rapidPlayerId);
    }

    @Override
    public Player save(Player player) {
        return playerJpaRepository.save(PlayerEntity.fromModel(player)).toModel();
    }

    @Override
    public List<Player> saveAll(List<Player> players) {
        List<PlayerEntity> playerEntities = players.stream().map(p -> PlayerEntity.fromModel(p)).collect(Collectors.toList());

        return playerJpaRepository.saveAll(playerEntities).stream().map(p -> p.toModel()).collect(Collectors.toList());
    }

    @Override
    public List<Player> findSinglePlayer() {
        return playerJpaRepository.findSinglePlayer().stream().map(PlayerEntity::toModel).collect(Collectors.toList());
    }
}
