package com.tennisfolio.Tennisfolio.player.infrastructure;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.PlayerJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.hibernate.annotations.NotFound;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository{

    private final PlayerJpaRepository playerJpaRepository;
    private final BufferedBatchSaver<PlayerEntity> bufferedBatchSaver;


    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository, TransactionTemplate transactionTemplate) {
        this.playerJpaRepository = playerJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(playerJpaRepository, 500, transactionTemplate);
    }

    @Override
    public Player getById(Long id) {
        return playerJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND))
                .toModel();
    }

    @Override
    public Player findByRapidPlayerId(String rapidPlayerId) {
        return playerJpaRepository.findByRapidPlayerId(rapidPlayerId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND))
                .toModel();
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
}
