package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingSyncService {

    private final StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate;
    private final RankingRepository rankingRepository;

    @Builder
    public RankingSyncService(StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> rankingApiTemplate, RankingRepository rankingRepository) {
        this.rankingApiTemplate = rankingApiTemplate;
        this.rankingRepository = rankingRepository;
    }

    @Transactional
    public void saveAtpRanking() {
        List<Ranking> rankingList = rankingApiTemplate.execute("");
        String lastUpdate = rankingList.stream()
                .findFirst()
                .map(Ranking::getLastUpdate)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        if(rankingRepository.existsByLastUpdate(lastUpdate)) return;

        rankingRepository.collect(rankingList);
        rankingRepository.flushAll();
    }
}
