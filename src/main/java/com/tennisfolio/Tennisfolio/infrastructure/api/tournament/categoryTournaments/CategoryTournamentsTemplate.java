package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CategoryTournamentsTemplate extends StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> {


    public CategoryTournamentsTemplate(
            ApiCaller apiCaller
            , ResponseParser<List<CategoryTournamentsDTO>> responseParser
            , EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> entityMapper
                                      ) {
        super(apiCaller, responseParser, entityMapper, RapidApi.CATEGORYTOURNAMENTS);

    }

}
