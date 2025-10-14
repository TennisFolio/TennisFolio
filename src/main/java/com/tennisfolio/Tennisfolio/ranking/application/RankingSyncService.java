package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
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

    private final ApiWorker apiWorker;
    private final RankingRepository rankingRepository;
    private final PlayerProvider playerProvider;

    @Builder
    public RankingSyncService(ApiWorker apiWorker, RankingRepository rankingRepository, PlayerProvider playerProvider) {
        this.apiWorker = apiWorker;
        this.rankingRepository = rankingRepository;
        this.playerProvider = playerProvider;
    }

    @Transactional
    public void saveAtpRanking() {
        List<Ranking> rankingList = apiWorker.process(RapidApi.ATPRANKINGS);
        String lastUpdate = rankingList.stream()
                .findFirst()
                .map(Ranking::getLastUpdate)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        if(rankingRepository.existsByLastUpdate(lastUpdate)) return;

        rankingList.stream().forEach(p -> {
            Player findPlayer = playerProvider.provide(p.getPlayer().getRapidPlayerId());
            p.updatePlayer(findPlayer);
        });

        rankingRepository.collect(rankingList);
        rankingRepository.flushAll();
    }
}
