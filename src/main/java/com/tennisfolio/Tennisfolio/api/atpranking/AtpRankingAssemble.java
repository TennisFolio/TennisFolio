package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.EntityAssembler;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AtpRankingAssemble implements EntityAssembler<List<AtpRankingApiDTO>, List<Ranking>> {
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    public AtpRankingAssemble(PlayerRepository playerRepository, PlayerService playerService){
        this.playerRepository = playerRepository;
        this.playerService = playerService;
    }
    @Override
    public List<Ranking> assemble(List<AtpRankingApiDTO> dto) {
        List<Ranking> rankList = dto.stream().map(rank -> {
            Player player = playerService.getOrCreatePlayerByRapidId(rank.getTeam().getPlayerRapidId());
            return new Ranking(rank, player);
        }).collect(Collectors.toList());

        return rankList;
    }
}
