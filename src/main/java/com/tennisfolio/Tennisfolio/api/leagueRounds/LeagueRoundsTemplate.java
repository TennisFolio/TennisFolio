package com.tennisfolio.Tennisfolio.api.leagueRounds;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueRoundsTemplate extends AbstractApiTemplate<List<LeagueRoundsDTO>, List<Round>> {
    private final ResponseParser<List<LeagueRoundsDTO>> leagueRoundsResponseParser;
    private final Mapper<List<LeagueRoundsDTO>, List<Round>> leagueRoundsMapper;
    private final SaveStrategy<List<Round>> leagueRoundsSaveStrategy;
    public LeagueRoundsTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("leagueRoundsResponseParser")ResponseParser<List<LeagueRoundsDTO>> leagueRoundsResponseParser
            , @Qualifier("leagueRoundsMapper") Mapper<List<LeagueRoundsDTO>, List<Round>> leagueRoundsMapper
            , @Qualifier("leagueRoundsSaveStrategy") SaveStrategy<List<Round>> leagueRoundsSaveStrategy) {
        super(decompressorUtil);
        this.leagueRoundsResponseParser = leagueRoundsResponseParser;
        this.leagueRoundsMapper = leagueRoundsMapper;
        this.leagueRoundsSaveStrategy = leagueRoundsSaveStrategy;
    }

    @Override
    public List<LeagueRoundsDTO> toDTO(String response) {
        return leagueRoundsResponseParser.parse(response);
    }

    @Override
    public List<Round> toEntity(List<LeagueRoundsDTO> dto, Object... params) {
        return leagueRoundsMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LEAGUEROUNDS.getParam(params);
    }

    @Override
    public List<Round> saveEntity(List<Round> entity) {
        return leagueRoundsSaveStrategy.save(entity);
    }
}
