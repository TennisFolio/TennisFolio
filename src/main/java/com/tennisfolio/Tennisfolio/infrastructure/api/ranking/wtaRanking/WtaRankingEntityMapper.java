package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.wtaRanking;

import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.dto.WtaRankingApiDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WtaRankingEntityMapper implements EntityMapper<List<WtaRankingApiDTO>, List<Ranking>> {
    @Override
    public List<Ranking> map(List<WtaRankingApiDTO> dto, Object... params) {
        List<Ranking> rankList = dto.stream().map(rank -> {
            Player player = Player.builder().rapidPlayerId(rank.getTeam().getPlayerRapidId()).build();

            return Ranking.builder()
                    .curRank(rank.getCurRank())
                    .bestRank(rank.getBestRank())
                    .preRank(rank.getPreRank())
                    .curPoints(rank.getPoint())
                    .prePoints(rank.getPrePoints())
                    .lastUpdate(rank.getUpdateTime())
                    .category(RankingCategory.WTA)
                    .player(player)
                    .build();
        }).collect(Collectors.toList());

        return rankList;
    }
}
