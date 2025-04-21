package com.tennisfolio.Tennisfolio.ranking.service;

import com.tennisfolio.Tennisfolio.api.atpranking.AtpRankingTemplate;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {
    private final AtpRankingTemplate rankingApiTemplate;
    private final RankingRepository rankingRepository;

    public RankingServiceImpl(AtpRankingTemplate rankingApiTemplate, RankingRepository rankingRepository){
        this.rankingApiTemplate = rankingApiTemplate;
        this.rankingRepository = rankingRepository;
    }

    @Override
    public void saveAtpRanking() {
        List<Ranking> rankings = rankingApiTemplate.execute("");
        rankingRepository.saveAll(rankings);

    }
}
