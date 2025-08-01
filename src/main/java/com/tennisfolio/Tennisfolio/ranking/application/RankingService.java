package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class RankingService {
    private final RankingRepository rankingRepository;
    @Builder
    public RankingService( RankingRepository rankingRepository){

        this.rankingRepository = rankingRepository;
    }

    public List<RankingResponse> getRanking(int page, int size) {
        List<Ranking> rankings = rankingRepository.findLatestRankings(PageRequest.of(page,size));

        return rankings.stream()
                       .map(RankingResponse::new)
                       .collect(Collectors.toList());

    }
}
