package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AtpRankingEntitySaver implements EntitySaver<List<Ranking>> {
    private final RankingRepository rankingRepository;

    public AtpRankingEntitySaver(RankingRepository rankingRepository){
        this.rankingRepository = rankingRepository;
    }

    @Override
    public List<Ranking> save(List<Ranking> entity) {
        if (entity.isEmpty()) {
            return Collections.emptyList();
        }

        String lastUpdateDate = entity.get(0).getLastUpdate();

        boolean alreadyExists = rankingRepository.findByLastUpdate(lastUpdateDate).isPresent();

        if (alreadyExists) {
            return Collections.emptyList(); // 또는 Optional.empty() / custom result
        }

        return rankingRepository.saveAll(entity);
    }
}
