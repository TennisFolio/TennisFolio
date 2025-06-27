package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.common.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CategoryTournamentsTemplate extends StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> {


    public CategoryTournamentsTemplate(ResponseParser<List<CategoryTournamentsDTO>> responseParser,
                                       EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> entityMapper,
                                       EntitySaver<List<Tournament>> entitySaver
                                      ) {
        super(responseParser, entityMapper, entitySaver, RapidApi.CATEGORYTOURNAMENTS);

    }

}
