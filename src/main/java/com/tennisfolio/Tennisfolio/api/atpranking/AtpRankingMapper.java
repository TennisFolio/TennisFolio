package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AtpRankingMapper implements Mapper<List<AtpRankingApiDTO>, List<Ranking>>{

    private final EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> assembler;

    public AtpRankingMapper(EntityAssemble<List<AtpRankingApiDTO>, List<Ranking>> assembler){
        this.assembler = assembler;
    }
    @Override
    public List<Ranking> map(List<AtpRankingApiDTO> dto, Object... params) {
        return assembler.assemble(dto, params);
    }
}
