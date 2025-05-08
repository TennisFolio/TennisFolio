package com.tennisfolio.Tennisfolio.api.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeagueEventsByRoundTemplate extends AbstractApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> {
    private final ResponseParser<List<LeagueEventsByRoundDTO>> leagueEventsByRoundResponseParser;
    private final Mapper<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundMapper;
    private final SaveStrategy<List<Match>> leagueEventsByRoundSaveStrategy;
    public LeagueEventsByRoundTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("leagueEventsByRoundResponseParser")ResponseParser<List<LeagueEventsByRoundDTO>> leagueEventsByRoundResponseParser
            , @Qualifier("leagueEventsByRoundMapper")Mapper<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundMapper
            , @Qualifier("leagueEventsByRoundSaveStrategy")SaveStrategy<List<Match>> leagueEventsByRoundSaveStrategy) {
        super(decompressorUtil);
        this.leagueEventsByRoundResponseParser = leagueEventsByRoundResponseParser;
        this.leagueEventsByRoundMapper = leagueEventsByRoundMapper;
        this.leagueEventsByRoundSaveStrategy = leagueEventsByRoundSaveStrategy;
    }

    @Override
    public List<LeagueEventsByRoundDTO> toDTO(String response) {
        return leagueEventsByRoundResponseParser.parse(response);
    }

    @Override
    public List<Match> toEntity(List<LeagueEventsByRoundDTO> dto, Object... params) {
        return leagueEventsByRoundMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LEAGUEEVENETBYROUND.getParam(params);
    }

    @Override
    public List<Match> saveEntity(List<Match> entity) {
        return leagueEventsByRoundSaveStrategy.save(entity);
    }
}
