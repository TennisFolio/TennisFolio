package com.tennisfolio.Tennisfolio.player.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.teamImage.TeamImage;
import com.tennisfolio.Tennisfolio.api.teamdetails.PlayerAggregate;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsTemplate;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService{
    private final AbstractApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate;
    private final PlayerRepository playerRepository;
    private final TeamImage teamImage;
    public PlayerServiceImpl(@Qualifier("teamDetailsTemplate") AbstractApiTemplate<TeamDetailsApiDTO, PlayerAggregate> teamDetailsTemplate
            , TeamImage teamImage
            , PlayerRepository playerRepository){
        this.teamDetailsTemplate = teamDetailsTemplate;
        this.teamImage = teamImage;
        this.playerRepository = playerRepository;
    }

    @Override
    public Player getOrCreatePlayerByRapidId(String rapidId) {
        return playerRepository.findByRapidPlayerId(rapidId)
                .orElseGet(() -> {
                    Player player = teamDetailsTemplate.execute(rapidId).getPlayer();
                    teamImage.saveImage(rapidId);

                    return player;
                });
    }

    @Override
    @Transactional
    public String saveTeamImage(String rapidId) {

        return teamImage.saveImage(rapidId);
    }
}
