package com.tennisfolio.Tennisfolio.prize.repository;

import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerPrizeJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.infrastructure.worker.BatchSaver;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Repository
public class PrizeRepositoryImpl implements PrizeRepository{
    private final PlayerPrizeJpaRepository playerPrizeJpaRepository;
    private final BufferedBatchSaver<PlayerPrizeEntity> bufferedBatchSaver;

    public PrizeRepositoryImpl(PlayerPrizeJpaRepository playerPrizeJpaRepository, TransactionTemplate transactionTemplate) {
        this.playerPrizeJpaRepository = playerPrizeJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(playerPrizeJpaRepository, 100, transactionTemplate);
    }

    @Override
    public List<PlayerPrize> findAll() {
        return playerPrizeJpaRepository.findAll().stream().map(PlayerPrizeEntity::toModel).toList();
    }

    @Override
    public PlayerPrize findByPlayer(Player player) {
        return null;
    }

    @Override
    public void save(PlayerPrize playerPrize) {
        playerPrizeJpaRepository.save(PlayerPrizeEntity.fromModel(playerPrize));
    }

    @Override
    public List<PlayerPrize> collect(PlayerPrize playerPrize) {
        return bufferedBatchSaver.collect(PlayerPrizeEntity.fromModel(playerPrize))
                .stream()
                .map(PlayerPrizeEntity::toModel)
                .toList();
    }

    @Override
    public List<PlayerPrize> collect(List<PlayerPrize> playerPrizes) {
        List<PlayerPrizeEntity> entities = playerPrizes.stream().map(PlayerPrizeEntity::fromModel).toList();
        return bufferedBatchSaver.collect(entities).stream().map(PlayerPrizeEntity::toModel).toList();
    }

    @Override
    public boolean flushWhenFull() {
        return bufferedBatchSaver.flushWhenFull();
    }

    @Override
    public boolean flushAll() {
        return bufferedBatchSaver.flushAll();
    }

    @Override
    public void flush() {
        playerPrizeJpaRepository.flush();
    }
}
