package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {
    private final RankingJpaRepository rankingJpaRepository;

    public RankingService( RankingJpaRepository rankingJpaRepository){

        this.rankingJpaRepository = rankingJpaRepository;
    }

    public List<RankingResponse> getRanking(String type) {
        List<Ranking> rankings = "init".equalsIgnoreCase(type)
                ? rankingJpaRepository.findLatestRankings(PageRequest.of(0, 20))
                : rankingJpaRepository.findLatestRankings();

        return rankings.stream()
                       .map(RankingResponse::new)
                       .collect(Collectors.toList());

    }
}
