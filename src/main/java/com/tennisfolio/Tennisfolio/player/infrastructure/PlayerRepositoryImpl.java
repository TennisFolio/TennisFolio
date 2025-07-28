package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository{

    private final PlayerJpaRepository playerJpaRepository;
    private final BufferedBatchSaver<PlayerEntity> bufferedBatchSaver;


    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository, TransactionTemplate transactionTemplate) {
        this.playerJpaRepository = playerJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(playerJpaRepository, 500, transactionTemplate);
    }

    @Override
    public PlayerEntity getById(Long id) {
        return playerJpaRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    @Override
    public Optional<PlayerEntity> findByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.findByRapidPlayerId(rapidPlayerId);
    }

    @Override
    public PlayerEntity save(PlayerEntity player) {
        return playerJpaRepository.save(player);
    }

    @Override
    public List<PlayerEntity> saveAll(List<PlayerEntity> players) {
        return playerJpaRepository.saveAll(players);
    }

    @Override
    public List<PlayerEntity> collect(PlayerEntity player) {
        return bufferedBatchSaver.collect(player);
    }

    @Override
    public List<PlayerEntity> collect(List<PlayerEntity> players) {
        return bufferedBatchSaver.collect(players);
    }

    @Override
    public boolean flushWhenFull() {
        return bufferedBatchSaver.flushWhenFull();
    }

    @Override
    public boolean flushAll() {
        return bufferedBatchSaver.flushAll();
    }
}
