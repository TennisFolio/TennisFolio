package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoTemplate extends StrategyApiTemplate<TournamentInfoDTO, Tournament> {


    public TournamentInfoTemplate(
              ApiCaller apiCaller
            , ResponseParser<TournamentInfoDTO> tournamentInfoResponseParser
            , EntityMapper<TournamentInfoDTO, Tournament> tournamentInfoEntityMapper
            ) {
        super(apiCaller, tournamentInfoResponseParser, tournamentInfoEntityMapper, RapidApi.TOURNAMENTINFO);
    }

}
