package com.tennisfolio.Tennisfolio.api.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoTemplate extends AbstractApiTemplate<TournamentInfoDTO, Tournament> {
    private final ResponseParser<TournamentInfoDTO> tournamentInfoResponseParser;
    private final Mapper<TournamentInfoDTO, Tournament> tournamentInfoMapper;
    private final SaveStrategy<Tournament> tournamentSaveStrategy;

    public TournamentInfoTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("tournamentInfoResponseParser")ResponseParser<TournamentInfoDTO> tournamentInfoResponseParser
            , @Qualifier("tournamentInfoMapper")Mapper<TournamentInfoDTO, Tournament> tournamentInfoMapper
            , @Qualifier("tournamentInfoSaveStrategy")SaveStrategy<Tournament> tournamentSaveStrategy) {
        super(decompressorUtil);
        this.tournamentInfoResponseParser = tournamentInfoResponseParser;
        this.tournamentInfoMapper = tournamentInfoMapper;
        this.tournamentSaveStrategy = tournamentSaveStrategy;
    }

    @Override
    public TournamentInfoDTO toDTO(String response) {
        return tournamentInfoResponseParser.parse(response);
    }

    @Override
    public Tournament toEntity(TournamentInfoDTO dto, Object... params) {
        return tournamentInfoMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.TOURNAMENTINFO.getParam(params);
    }

    @Override
    public Tournament saveEntity(Tournament entity) {
        return tournamentSaveStrategy.save(entity);
    }
}
