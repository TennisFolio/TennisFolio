package com.tennisfolio.Tennisfolio.prize.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;

import java.util.List;

public interface PrizeRepository {
    List<PlayerPrize> findAll();
    PlayerPrize findByPlayer(Player player);
    void save(PlayerPrize playerPrize);
    List<PlayerPrize> collect(PlayerPrize playerPrize);
    List<PlayerPrize> collect(List<PlayerPrize> playerPrizes);
    boolean flushWhenFull();
    boolean flushAll();
    void flush();
}
