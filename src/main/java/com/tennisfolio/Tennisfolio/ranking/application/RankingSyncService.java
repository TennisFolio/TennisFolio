package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingSyncService {

    private final StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate;
    private final RankingRepository rankingRepository;

    public RankingSyncService(StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate, RankingRepository rankingRepository) {
        this.rankingApiTemplate = rankingApiTemplate;
        this.rankingRepository = rankingRepository;
    }

    @Transactional
    public void saveAtpRanking() {
        List<Ranking> rankingList = rankingApiTemplate.execute("");
        List<RankingEntity> rankingEntityList = rankingList.stream().map(p -> RankingEntity.fromModel(p)).toList();
        rankingRepository.saveAll(rankingEntityList);
    }
}
