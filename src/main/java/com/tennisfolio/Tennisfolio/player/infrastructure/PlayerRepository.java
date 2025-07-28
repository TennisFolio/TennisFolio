package com.tennisfolio.Tennisfolio.player.infrastructure;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    PlayerEntity getById(Long id);

    Optional<PlayerEntity> findByRapidPlayerId(String rapidPlayerId);

    PlayerEntity save(PlayerEntity player);

    List<PlayerEntity> saveAll(List<PlayerEntity> players);

    List<PlayerEntity> collect(PlayerEntity player);

    List<PlayerEntity> collect(List<PlayerEntity> players);

    boolean flushWhenFull();

    boolean flushAll();

}

