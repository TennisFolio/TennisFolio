package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoTemplate extends StrategyApiTemplate<TournamentInfoDTO, Tournament> {


    public TournamentInfoTemplate(
              ResponseParser<TournamentInfoDTO> tournamentInfoResponseParser
            , EntityMapper<TournamentInfoDTO, Tournament> tournamentInfoEntityMapper
            ) {
        super(tournamentInfoResponseParser, tournamentInfoEntityMapper, RapidApi.TOURNAMENTINFO);
    }

}
