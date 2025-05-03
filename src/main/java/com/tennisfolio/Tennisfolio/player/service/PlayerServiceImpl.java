package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.teamdetails.PlayerAggregate;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsTemplate;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl implements PlayerService{
    private final AbstractApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate;
    private final PlayerRepository playerRepository;
    public PlayerServiceImpl(@Qualifier("teamDetailsTemplate") AbstractApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate, PlayerRepository playerRepository){
        this.teamDetailsTemplate = teamDetailsTemplate;
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getOrCreatePlayerByRapidId(String rapidId) {
        return playerRepository.findByRapidPlayerId(rapidId)
                .orElseGet(() -> (teamDetailsTemplate.execute(rapidId)).getPlayer());
    }
}
