package com.tennisfolio.Tennisfolio.ranking.service;

import com.tennisfolio.Tennisfolio.ranking.response.RankingResponse;

import java.util.List;

public interface RankingService {
    public void saveAtpRanking();
    public List<RankingResponse> getRanking(String type);
}
