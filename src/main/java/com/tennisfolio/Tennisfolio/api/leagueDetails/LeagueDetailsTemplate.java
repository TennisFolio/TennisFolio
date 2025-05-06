package com.tennisfolio.Tennisfolio.api.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsTemplate extends AbstractApiTemplate<LeagueDetailsDTO, Tournament> {
    private final Mapper<LeagueDetailsDTO, Tournament> leagueDetailsMapper;
    private final ResponseParser<LeagueDetailsDTO> leagueDetailsParser;
    private final SaveStrategy<Tournament> leagueDetailsSaveStrategy;
    public LeagueDetailsTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("leagueDetailsMapper") Mapper<LeagueDetailsDTO, Tournament> leagueDetailsMapper
            , @Qualifier("leagueDetailsResponseParser") ResponseParser<LeagueDetailsDTO> leagueDetailsParser
            , @Qualifier("leagueDetailsSaveStrategy") SaveStrategy<Tournament> leagueDetailsSaveStrategy) {
        super(decompressorUtil);
        this.leagueDetailsMapper = leagueDetailsMapper;
        this.leagueDetailsParser = leagueDetailsParser;
        this.leagueDetailsSaveStrategy = leagueDetailsSaveStrategy;
    }

    @Override
    public LeagueDetailsDTO toDTO(String response) {
        return leagueDetailsParser.parse(response);
    }

    @Override
    public Tournament toEntity(LeagueDetailsDTO dto, Object... params) {
        return leagueDetailsMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LEAGUEDETAILS.getParam(params);
    }

    @Override
    public Tournament saveEntity(Tournament entity) {
        return leagueDetailsSaveStrategy.save(entity);
    }
}
