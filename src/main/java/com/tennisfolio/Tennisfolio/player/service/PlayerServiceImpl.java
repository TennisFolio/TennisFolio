package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsTemplate;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl implements PlayerService{
    private final TeamDetailsTemplate teamDetailsTemplate;
    private final PlayerRepository playerRepository;
    public PlayerServiceImpl(TeamDetailsTemplate teamDetailsTemplate, PlayerRepository playerRepository){
        this.teamDetailsTemplate = teamDetailsTemplate;
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getOrCreatePlayerByRapidId(String rapidId) {
        return playerRepository.findByRapidPlayerId(rapidId)
                .orElseGet(() -> {
                    Player player = teamDetailsTemplate.execute(rapidId);
                    return playerRepository.save(player);
                });

    }
}
