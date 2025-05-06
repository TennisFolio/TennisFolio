package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtpRankingTemplate extends AbstractApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> {

    private final ResponseParser<List<AtpRankingApiDTO>> atpRankingResponseParser;

    private final Mapper<List<AtpRankingApiDTO>, List<Ranking>> atpRankingMapper;

    private final SaveStrategy<List<Ranking>> atpRankingSaveStrategy;

    public AtpRankingTemplate(DecompressorUtil decompressorUtil,
                              @Qualifier("atpRankingResponseParser") ResponseParser<List<AtpRankingApiDTO>> atpRankingResponseParser,
                              @Qualifier("atpRankingMapper") Mapper<List<AtpRankingApiDTO>, List<Ranking>> atpRankingMapper,
                              @Qualifier("atpRankingSaveStrategy") SaveStrategy<List<Ranking>> atpRankingSaveStrategy) {
        super(decompressorUtil);
        this.atpRankingSaveStrategy = atpRankingSaveStrategy;
        this.atpRankingResponseParser = atpRankingResponseParser;
        this.atpRankingMapper = atpRankingMapper;
    }


    @Override
    public List<AtpRankingApiDTO> toDTO(String response) {
        return  atpRankingResponseParser.parse(response);
    }

    @Override
    public List<Ranking> toEntity(List<AtpRankingApiDTO> dto, Object... params) {
        return atpRankingMapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.ATPRANKINGS.getParam(params);
    }

    @Override
    public List<Ranking> saveEntity(List<Ranking> entity) {
        return atpRankingSaveStrategy.save(entity);
    }

}
