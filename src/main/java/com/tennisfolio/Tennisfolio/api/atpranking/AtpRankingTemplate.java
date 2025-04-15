package com.tennisfolio.Tennisfolio.api.atpranking;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtpRankingTemplate extends AbstractApiTemplate<List<AtpRankingApiDTO>, List<Ranking>> {
    private final AtpRankingResponseParser parser;
    private final AtpRankingMapper mapper;

    public AtpRankingTemplate(DecompressorUtil decompressorUtil, AtpRankingResponseParser parser, AtpRankingMapper mapper) {
        super(decompressorUtil);
        this.parser = parser;
        this.mapper = mapper;
    }


    @Override
    public List<AtpRankingApiDTO> toDTO(String response) {
        return parser.parse(response);
    }

    @Override
    public List<Ranking> toEntity(List<AtpRankingApiDTO> dto) {
        return mapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.ATPRANKINGS.getParam(params);
    }
}
