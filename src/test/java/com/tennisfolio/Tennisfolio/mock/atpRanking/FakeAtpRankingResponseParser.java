package com.tennisfolio.Tennisfolio.mock.atpRanking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;

import java.util.List;

public class FakeAtpRankingResponseParser implements ResponseParser<List<AtpRankingApiDTO>> {
    @Override
    public List<AtpRankingApiDTO> parse(String response) {
        return null;
    }
}
