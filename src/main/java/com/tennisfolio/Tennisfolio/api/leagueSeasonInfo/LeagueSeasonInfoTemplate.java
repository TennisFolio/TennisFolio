package com.tennisfolio.Tennisfolio.api.leagueSeasonInfo;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LeagueSeasonInfoTemplate extends AbstractApiTemplate<LeagueSeasonInfoDTO, Season> {
    private final ResponseParser<LeagueSeasonInfoDTO> leagueSeasonInfoParser;
    private final Mapper<LeagueSeasonInfoDTO,Season> leagueSeasonInfoMapper;
    private final SaveStrategy<Season> leagueSeasonInfoSaveStrategy;

    public LeagueSeasonInfoTemplate(DecompressorUtil decompressorUtil
            , @Qualifier("leagueSeasonInfoResponseParser")ResponseParser<LeagueSeasonInfoDTO> leagueSeasonInfoParser
            , @Qualifier("leagueSeasonInfoMapper")Mapper<LeagueSeasonInfoDTO, Season> leagueSeasonInfoMapper
            , @Qualifier("leagueSeasonInfoSaveStrategy")SaveStrategy<Season> leagueSeasonInfoSaveStrategy) {
        super(decompressorUtil);
        this.leagueSeasonInfoParser = leagueSeasonInfoParser;
        this.leagueSeasonInfoMapper = leagueSeasonInfoMapper;
        this.leagueSeasonInfoSaveStrategy = leagueSeasonInfoSaveStrategy;
    }

    @Override
    public LeagueSeasonInfoDTO toDTO(String response) {
        return leagueSeasonInfoParser.parse(response);
    }

    @Override
    public Season toEntity(LeagueSeasonInfoDTO dto, Object... params) {
        return leagueSeasonInfoMapper.map(dto, params);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.LEAGUESEASONINFO.getParam(params);
    }

    @Override
    public Season saveEntity(Season entity) {
        return leagueSeasonInfoSaveStrategy.save(entity);
    }
}
