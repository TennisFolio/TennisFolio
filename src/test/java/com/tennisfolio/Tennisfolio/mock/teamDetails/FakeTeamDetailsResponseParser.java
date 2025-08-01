package com.tennisfolio.Tennisfolio.mock.teamDetails;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ResponseParser;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;

public class FakeTeamDetailsResponseParser implements ResponseParser<TeamDetailsApiDTO> {
    @Override
    public TeamDetailsApiDTO parse(String response) {
        return new TeamDetailsApiDTO();
    }
}
