package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingSyncService {

    private final StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate;

    public RankingSyncService(StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate) {
        this.rankingApiTemplate = rankingApiTemplate;
    }

    public void saveAtpRanking() {
        rankingApiTemplate.execute("");
    }
}
