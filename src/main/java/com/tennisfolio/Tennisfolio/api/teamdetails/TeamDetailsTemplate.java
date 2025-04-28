package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.api.base.Mapper;
import com.tennisfolio.Tennisfolio.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsTemplate extends AbstractApiTemplate<TeamDetailsApiDTO, Player> {

    private final Mapper mapper;

    private final ResponseParser parser;

    public TeamDetailsTemplate(DecompressorUtil decompressorUtil,
                               @Qualifier("teamDetailsMapper") TeamDetailsMapper mapper,
                               @Qualifier("teamDetailsResponseParser")TeamDetailsResponseParser parser){
        super(decompressorUtil);
        this.mapper = mapper;
        this.parser = parser;
    }

    @Override
    public TeamDetailsApiDTO toDTO(String response) {
        return (TeamDetailsApiDTO) parser.parse(response);
    }

    @Override
    public Player toEntity(TeamDetailsApiDTO dto) {
        return (Player)mapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.TEAMDETAILS.getParam(params);
    }
}
