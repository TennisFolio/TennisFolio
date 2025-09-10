package com.tennisfolio.Tennisfolio.mock.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails.LeagueDetailsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;

public class FakeLeagueDetails extends StrategyApiTemplate<LeagueDetailsDTO, Tournament> {
    public FakeLeagueDetails(ApiCaller apiCaller, ResponseParser<LeagueDetailsDTO> parser, EntityMapper<LeagueDetailsDTO, Tournament> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, endpoint);
    }
}
