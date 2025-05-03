package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.player.domain.Player;

public interface PlayerService {
    Player getOrCreatePlayerByRapidId(String rapidId);
    String saveTeamImage(String rapidId);
}
