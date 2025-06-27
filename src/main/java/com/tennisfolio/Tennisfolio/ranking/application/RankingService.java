package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {
    private final RankingRepository rankingRepository;

    public RankingService( RankingRepository rankingRepository){

        this.rankingRepository = rankingRepository;
    }

    public List<RankingResponse> getRanking(String type) {
        List<Ranking> rankings = "init".equalsIgnoreCase(type)
                ? rankingRepository.findLatestRankings(PageRequest.of(0, 20))
                : rankingRepository.findLatestRankings();

        return rankings.stream()
                       .map(RankingResponse::new)
                       .collect(Collectors.toList());

    }
}
