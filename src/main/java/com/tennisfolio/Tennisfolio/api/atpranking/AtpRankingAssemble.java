package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AtpRankingAssemble implements EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> {

    private final PlayerService playerService;
    public AtpRankingAssemble(PlayerService playerService){
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
