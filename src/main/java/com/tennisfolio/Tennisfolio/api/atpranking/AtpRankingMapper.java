package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.EntityAssembler;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AtpRankingMapper implements Mapper<List<AtpRankingApiDTO>, List<Ranking>>{

    private final EntityAssembler<List<AtpRankingApiDTO>, List<Ranking>> assembler;

    public AtpRankingMapper(EntityAssembler<List<AtpRankingApiDTO>, List<Ranking>> assembler){
        this.assembler = assembler;
    }
    @Override
    public List<Ranking> map(List<AtpRankingApiDTO> dto) {
        return assembler.assemble(dto);
    }
}
