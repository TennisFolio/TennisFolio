package com.tennisfolio.Tennisfolio.api.teamdetails;

import com.tennisfolio.Tennisfolio.api.base.*;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TeamDetailsTemplate extends AbstractApiTemplate<TeamDetailsApiDTO, PlayerAggregate> {

    private final Mapper<TeamDetailsApiDTO, PlayerAggregate> mapper;

    private final ResponseParser<TeamDetailsApiDTO> parser;

    private final SaveStrategy<PlayerAggregate> saveStrategy;

    public TeamDetailsTemplate(DecompressorUtil decompressorUtil,
                               @Qualifier("teamDetailsMapper") Mapper<TeamDetailsApiDTO, PlayerAggregate> mapper,
                               @Qualifier("teamDetailsResponseParser")ResponseParser<TeamDetailsApiDTO> parser,
                               @Qualifier("playerAndPrizeSaveStrategy") SaveStrategy<PlayerAggregate> saveStrategy
    ){
        super(decompressorUtil);
        this.mapper = mapper;
        this.parser = parser;
        this.saveStrategy = saveStrategy;
    }

    @Override
    public TeamDetailsApiDTO toDTO(String response) {
        return (TeamDetailsApiDTO) parser.parse(response);
    }

    @Override
    public PlayerAggregate toEntity(TeamDetailsApiDTO dto) {
        return (PlayerAggregate)mapper.map(dto);
    }

    @Override
    public String getEndpointUrl(Object... params) {
        return RapidApi.TEAMDETAILS.getParam(params);
    }

    @Override
    public PlayerAggregate saveEntity(PlayerAggregate entity) {
        return (PlayerAggregate)saveStrategy.save(entity);
    }
}
