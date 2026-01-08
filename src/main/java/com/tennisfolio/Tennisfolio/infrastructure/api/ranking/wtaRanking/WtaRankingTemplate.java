package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.wtaRanking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import com.tennisfolio.Tennisfolio.ranking.dto.WtaRankingApiDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WtaRankingTemplate extends StrategyApiTemplate<List<WtaRankingApiDTO>, List<Ranking>> {
    public WtaRankingTemplate(ApiCaller apiCaller,
                              ResponseParser<List<WtaRankingApiDTO>> parser,
                              EntityMapper<List<WtaRankingApiDTO>, List<Ranking>> mapper,
                              ApiCallCounter apiCallCounter) {
        super(apiCaller, parser, mapper, apiCallCounter, RapidApi.WTARANKINGS);
    }
}
