package com.tennisfolio.Tennisfolio.mock.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo.TournamentInfoDTO;

import java.util.List;

public class FakeTournamentInfo extends StrategyApiTemplate<TournamentInfoDTO, Tournament> {
    public FakeTournamentInfo(ApiCaller apiCaller, ResponseParser<TournamentInfoDTO> parser, EntityMapper<TournamentInfoDTO, Tournament> mapper, ApiCallCounter apiCallCounter, RapidApi endpoint) {
        super(apiCaller, parser, mapper, apiCallCounter, endpoint);
    }
}
