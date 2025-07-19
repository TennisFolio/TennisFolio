package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntitySaver;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RankingJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AtpRankingEntitySaver implements EntitySaver<List<Ranking>> {
    private final RankingJpaRepository rankingJpaRepository;

    public AtpRankingEntitySaver(RankingJpaRepository rankingJpaRepository){
        this.rankingJpaRepository = rankingJpaRepository;
    }

    @Override
    public List<Ranking> save(List<Ranking> entity) {
        if (entity.isEmpty()) {
            return Collections.emptyList();
        }

        String lastUpdateDate = entity.get(0).getLastUpdate();

        boolean alreadyExists = !rankingJpaRepository.findByLastUpdate(lastUpdateDate).isEmpty();

        if (alreadyExists) {
            return Collections.emptyList(); // 또는 Optional.empty() / custom result
        }

        return rankingJpaRepository.saveAll(entity);
    }
}
