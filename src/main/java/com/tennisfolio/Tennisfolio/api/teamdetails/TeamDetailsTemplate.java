package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.base.DecompressorUtil;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsTemplate extends AbstractApiTemplate<TeamDetailsApiDTO, Player> {

    private final TeamDetailsMapper mapper;
    private final TeamDetailsResponseParser parser;

    public TeamDetailsTemplate(DecompressorUtil decompressorUtil, TeamDetailsMapper mapper, TeamDetailsResponseParser parser){
        super(decompressorUtil);
        this.mapper = mapper;
        this.parser = parser;
    }

    @Override
    public TeamDetailsApiDTO toDTO(String response) {
        return parser.parse(response);
    }

    @Override
    public Player toEntity(TeamDetailsApiDTO dto) {
        return mapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.TEAMDETAILS.getParam(params);
    }
}
