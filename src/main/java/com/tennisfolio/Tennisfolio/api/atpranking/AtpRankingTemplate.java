package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtpRankingTemplate extends AbstractApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> {

    private final ResponseParser atpRankingResponseParser;

    private final Mapper atpRankingMapper;

    public AtpRankingTemplate(DecompressorUtil decompressorUtil,
                              @Qualifier("atpRankingResponseParser") ResponseParser atpRankingResponseParser,
                              @Qualifier("atpRankingMapper") Mapper atpRankingMapper) {
        super(decompressorUtil);
        this.atpRankingResponseParser = atpRankingResponseParser;
        this.atpRankingMapper = atpRankingMapper;
    }


    @Override
    public List<AtpRankingApiDTO> toDTO(String response) {
        return (List<AtpRankingApiDTO>) atpRankingResponseParser.parse(response);
    }

    @Override
    public List<Ranking> toEntity(List<AtpRankingApiDTO> dto) {
        return (List<Ranking>) atpRankingMapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.ATPRANKINGS.getParam(params);
    }
}
