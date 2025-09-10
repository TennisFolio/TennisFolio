package com.tennisfolio.Tennisfolio.mock.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.*;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;

import java.util.List;

public class FakeCategoryTournamentsApiTemplate extends StrategyApiTemplate<List<CategoryTournamentsDTO>, List<Tournament>> {
    public FakeCategoryTournamentsApiTemplate(ApiCaller apiCaller, ResponseParser<List<CategoryTournamentsDTO>> parser, EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> mapper, RapidApi endpoint) {
        super(apiCaller, parser, mapper, endpoint);
    }
}
