package com.tennisfolio.Tennisfolio.mock.atpRanking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;

import java.util.List;

public class FakeAtpRankingApiTemplate extends StrategyApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> {
    public FakeAtpRankingApiTemplate(ApiCaller apiCaller, ResponseParser<List<AtpRankingApiDTO>> parser, EntityMapper<List<AtpRankingApiDTO>,
            List<Ranking>> mapper, ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
