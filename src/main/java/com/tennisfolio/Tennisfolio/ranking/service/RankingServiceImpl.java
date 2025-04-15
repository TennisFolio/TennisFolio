package com.tennisfolio.Tennisfolio.ranking.service;

import com.tennisfolio.Tennisfolio.api.atpranking.AtpRankingTemplate;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {
    private final AtpRankingTemplate rankingApiTemplate;

    public RankingServiceImpl(AtpRankingTemplate rankingApiTemplate){
        this.rankingApiTemplate = rankingApiTemplate;
    }

    @Override
    public void atpRanking() {
        List<Ranking> rankings = rankingApiTemplate.execute("");
    }
}
