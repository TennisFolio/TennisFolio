package com.tennisfolio.Tennisfolio.mock.categoryTournaments;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.fixtures.TournamentFixtures;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityMapper;
import com.tennisfolio.Tennisfolio.infrastructure.api.tournament.categoryTournaments.CategoryTournamentsDTO;

import java.util.ArrayList;
import java.util.List;

public class FakeCategoryTournamentMapper implements EntityMapper<List<CategoryTournamentsDTO>, List<Tournament>> {
    @Override
    public List<Tournament> map(List<CategoryTournamentsDTO> dto, Object... params) {
        Tournament rorlandGarros = TournamentFixtures.rolandGarrosATP();
        Tournament wimbledon = TournamentFixtures.wimbledonATP();

        List<Tournament> tournamentList = new ArrayList<>();
        tournamentList.add(rorlandGarros);
        tournamentList.add(wimbledon);
        return tournamentList;
    }
}
