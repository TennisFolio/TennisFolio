package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.tournamentInfo;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.CategoryJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import org.springframework.stereotype.Component;

@Component
public class TournamentInfoAssemble implements EntityAssemble<TournamentInfoDTO, Tournament> {
    private final TournamentRepository tournamentRepository;

    public TournamentInfoAssemble(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }


    @Override
    public Tournament assemble(TournamentInfoDTO dto, Object... params) {
        Tournament findTournament =  tournamentRepository.findWithCategoryByRapidTournamentId(dto.getTournament().getRapidId())
        .orElse(Tournament.builder().rapidTournamentId(dto.getTournament().getRapidId()).build());

        findTournament.updateFromTournamentInfo(dto.getCity(), dto.getMatchType(), dto.getGroundType());

        return findTournament;

    }
}
