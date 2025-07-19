package com.tennisfolio.Tennisfolio.player.repository;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository{

    private final PlayerJpaRepository playerJpaRepository;
    private final BufferedBatchSaver<Player> playerBufferedBatchSaver;

    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository) {
        this.playerJpaRepository = playerJpaRepository;
        this.playerBufferedBatchSaver = new BufferedBatchSaver<>(playerJpaRepository, 500);
    }

    @Override
    public Player getById(Long id) {
        return playerJpaRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    @Override
    public Optional<Player> findByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.findByRapidPlayerId(rapidPlayerId);
    }

    @Override
    public Player save(Player player) {
        return playerJpaRepository.save(player);
    }

    @Override
    public List<Player> saveAll(List<Player> players) {
        return playerJpaRepository.saveAll(players);
    }

    @Override
    public List<Player> bufferedSave(Player player) {
        return playerBufferedBatchSaver.collect(player);
    }

    @Override
    public List<Player> bufferedSaveAll(List<Player> players) {
        return playerBufferedBatchSaver.collect(players);
    }

    @Override
    public List<Player> flush() {

        return playerBufferedBatchSaver.flush();
    }
}
