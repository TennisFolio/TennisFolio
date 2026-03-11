package com.tennisfolio.Tennisfolio.ranking.application;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

        List<Ranking> findAtpRanking = rankingRepository.findByLastUpdateAndCategory(lastUpdate, RankingCategory.ATP);

        if(!findAtpRanking.isEmpty()) return;

        rankingList.stream().forEach(p -> {
            Player findPlayer = playerProvider.provide(p.getPlayer().getRapidPlayerId());
            p.updatePlayer(findPlayer);
        });

        rankingRepository.collect(rankingList);
        rankingRepository.flushAll();
    }

    @Transactional
    public void saveWtaRanking() {
        List<Ranking> rankingList = apiWorker.process(RapidApi.WTARANKINGS);

        String lastUpdate = rankingList.stream()
                .findFirst()
                .map(Ranking::getLastUpdate)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        // 2. 이전 업데이트 날짜 조회
        Optional<String> beforeLastUpdate =
                rankingRepository.findTopLastUpdateByCategoryOrderByLastUpdateDesc(RankingCategory.WTA);

        // 3. 이미 저장된 데이터라면 종료
        if (beforeLastUpdate.isPresent() && beforeLastUpdate.get().equals(lastUpdate)) {
            return;
        }

        // 4. 이전 ranking 조회
        List<Ranking> beforeRankings = beforeLastUpdate
                .map(date -> rankingRepository.findByLastUpdateAndCategory(date, RankingCategory.WTA))
                .orElseGet(List::of);


        // 5. 이전 ranking Map 변환
        Map<Long, Ranking> beforeRankingMap =
                beforeRankings.stream()
                        .collect(Collectors.toMap(
                                r -> r.getPlayer().getPlayerId(),
                                r -> r
                        ));

        // 6. ranking 데이터 가공
        for(Ranking ranking : rankingList){

            Player findPlayer = playerProvider.provide(ranking.getPlayer().getRapidPlayerId());
            Ranking before = beforeRankingMap.get(findPlayer.getPlayerId());

            if(before != null){
                ranking.applyPreviousPoints(before.getCurPoints());
            }

            ranking.updatePlayer(findPlayer);
        }

        rankingRepository.collect(rankingList);
        rankingRepository.flushAll();

    }
}
