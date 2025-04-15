package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.player.domain.Player;

public interface PlayerService {
    public Player getOrCreatePlayerByRapidId(String rapidId);
}
