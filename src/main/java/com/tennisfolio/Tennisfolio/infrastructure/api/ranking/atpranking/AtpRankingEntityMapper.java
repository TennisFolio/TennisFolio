package com.tennisfolio.Tennisfolio.infrastructure.api.ranking.atpranking;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import com.tennisfolio.Tennisfolio.ranking.repository.RankingEntity;
import com.tennisfolio.Tennisfolio.ranking.dto.AtpRankingApiDTO;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AtpRankingEntityMapper implements EntityMapper<List<AtpRankingApiDTO>, List<Ranking>> {

    private final EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> assembler;

    public AtpRankingEntityMapper(EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> assembler){
        this.assembler = assembler;
    }
    @Override
    public List<Ranking> map(List<AtpRankingApiDTO> dto, Object... params) {
        return assembler.assemble(dto, params);
    }
}
