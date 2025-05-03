package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.SaveStrategy;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AtpRankingSaveStrategy implements SaveStrategy<List<Ranking>> {
    private final RankingRepository rankingRepository;

    public AtpRankingSaveStrategy(RankingRepository rankingRepository){
        this.rankingRepository = rankingRepository;
    }

    @Override
    public List<Ranking> save(List<Ranking> entity) {
        List<Long> curRanks = entity.stream().map(Ranking::getCurRank).distinct().toList();
        List<String> updates = entity.stream().map(Ranking::getLastUpdate).distinct().toList();

        List<Ranking> duplicated = rankingRepository.findDuplicateRanking(curRanks, updates);

        Map<String, Long> rankingMap = duplicated.stream()
                .collect(Collectors.toMap(
                    r -> r.getCurRank() + "|" + r.getLastUpdate(),
                        Ranking::getRankingId
                ));

        entity.forEach(r -> {
            String key = r.getCurRank() + "|" + r.getLastUpdate();
            if(rankingMap.containsKey(key)){
                r.setRankingId(rankingMap.get(key));
            }
        });

        return rankingRepository.saveAll(entity);
    }
}
