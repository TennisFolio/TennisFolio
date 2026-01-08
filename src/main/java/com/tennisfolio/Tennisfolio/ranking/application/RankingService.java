package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.common.RankingSearchCondition;
import com.tennisfolio.Tennisfolio.player.domain.Country;
import com.tennisfolio.Tennisfolio.player.dto.CountryResponse;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingQueryRepository;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {
    private final RankingRepository rankingRepository;
    @Builder
    public RankingService(RankingRepository rankingRepository){

        this.rankingRepository = rankingRepository;
    }

    public List<RankingResponse> getRanking(int page, int size, RankingSearchCondition condition, String keyword) {
        List<Ranking> rankings = rankingRepository.findLatestRankings(PageRequest.of(page,size));

        return rankings.stream()
                       .map(RankingResponse::new)
                       .collect(Collectors.toList());

    }

    public List<RankingResponse> getRankingBefore(int page, int size){
        List<Ranking> rankings = rankingRepository.findLatestRankingsBefore(PageRequest.of(page,size));

        return rankings.stream()
                .map(RankingResponse::new)
                .collect(Collectors.toList());

    }

    public List<RankingResponse> getRankingSearchByCondition(int page, int size, RankingCategory category, String country, String name){
        Pageable pageable = PageRequest.of(page, size);
        Page<Ranking> rankings = rankingRepository.search(pageable, category, country, name);

        return rankings.stream()
                .map(RankingResponse::new)
                .collect(Collectors.toList());
    }

    public List<CountryResponse> getDistinctTopRankedCountries(){
        List<Country> countries = rankingRepository.getDistinctCountriesFromTopRankings();

        return countries.stream().map(p -> new CountryResponse(p.getCountryCode(), p.getCountryName())).toList();
    }

}
