package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class LeagueDetailsAssemble implements EntityAssemble<LeagueDetailsDTO, Tournament> {
    private final TournamentRepository tournamentRepository;
    private final PlayerService playerService;

    public LeagueDetailsAssemble(TournamentRepository tournamentRepository, PlayerService playerService) {
        this.tournamentRepository = tournamentRepository;
        this.playerService = playerService;
    }


    @Override
    public Tournament assemble(LeagueDetailsDTO dto, Object... params) {
        Tournament tournament = tournamentRepository.findWithCategoryByRapidTournamentId(dto.getRapidId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        Player mostTitlePlayer = null;
        if (dto.getMostTitlePlayerRapidId().isPresent()) {
            mostTitlePlayer = playerService.getOrCreatePlayerByRapidId(dto.getMostTitlePlayerRapidId().get());
        }

        Player titleHolder = null;
        if (dto.getTitleHolder() != null && dto.getTitleHolder().getRapidId() != null) {
            titleHolder = playerService.getOrCreatePlayerByRapidId(dto.getTitleHolder().getRapidId());
        }
        tournament.updateFromLeagueDetails(mostTitlePlayer, titleHolder, dto);

        return tournament;
    }
}
