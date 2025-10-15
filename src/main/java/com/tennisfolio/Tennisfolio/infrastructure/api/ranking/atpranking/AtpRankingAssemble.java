package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerRepository;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AtpRankingAssemble implements EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    public AtpRankingAssemble(PlayerService playerService, PlayerRepository playerRepository){
        this.playerService = playerService;
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Ranking> assemble(List<AtpRankingApiDTO> dto, Object... params) {

        List<Ranking> rankList = dto.stream().map(rank -> {
            Player player = Player.builder().rapidPlayerId(rank.getTeam().getPlayerRapidId()).build();

            return new Ranking(rank, player);
        }).collect(Collectors.toList());

        return rankList;
    }
}
