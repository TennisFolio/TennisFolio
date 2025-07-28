package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtpRankingTemplate extends StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> {


    public AtpRankingTemplate(
                              ResponseParser<List<AtpRankingApiDTO>> atpRankingResponseParser,
                              EntityMapper<List<AtpRankingApiDTO>, List<Ranking>> atpRankingEntityMapper
                              ) {
        super(atpRankingResponseParser,atpRankingEntityMapper,RapidApi.ATPRANKINGS);

    }

}
