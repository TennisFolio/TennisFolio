package com.tennisfolio.Tennisfolio.api.leagueSeasons;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LeagueSeasonsTemplate extends AbstractApiTemplate<List<LeagueSeasonsDTO>, List<Season>> {
    private final ResponseParser<List<LeagueSeasonsDTO>> leagueSeasonsParser;
    private final Mapper<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsMapper;
    private final SaveStrategy<List<Season>> leagueSeasonsSaveStrategy;
    public LeagueSeasonsTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("leagueSeasonsResponseParser") ResponseParser<List<LeagueSeasonsDTO>> leagueSeasonsParser
            , @Qualifier("leagueSeasonsMapper") Mapper<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsMapper
            , @Qualifier("leagueSeasonsSaveStrategy") SaveStrategy<List<Season>> leagueSeasonsSaveStrategy) {
        super(decompressorUtil);
        this.leagueSeasonsParser = leagueSeasonsParser;
        this.leagueSeasonsMapper = leagueSeasonsMapper;
        this.leagueSeasonsSaveStrategy = leagueSeasonsSaveStrategy;
    }

    @Override
    public List<LeagueSeasonsDTO> toDTO(String response) {
        return leagueSeasonsParser.parse(response);
    }

    @Override
    public List<Season> toEntity(List<LeagueSeasonsDTO> dto, Object... params) {
        return leagueSeasonsMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LEAGUESEASONS.getParam(params);
    }

    @Override
    public List<Season> saveEntity(List<Season> entity) {
        return leagueSeasonsSaveStrategy.save(entity);
    }
}
