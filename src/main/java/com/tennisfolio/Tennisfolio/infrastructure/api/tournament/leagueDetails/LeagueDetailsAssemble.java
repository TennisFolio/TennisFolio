package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LeagueDetailsAssemble implements EntityAssemble<LeagueDetailsDTO, Tournament> {
    private final TournamentJpaRepository tournamentJpaRepository;
    private final PlayerService playerService;

    public LeagueDetailsAssemble(TournamentJpaRepository tournamentJpaRepository, PlayerService playerService) {
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.playerService = playerService;
    }

    @Override
    public Tournament assemble(LeagueDetailsDTO dto, Object... params) {
        Optional<Tournament> optionalTournament = tournamentJpaRepository.findByRapidTournamentId(dto.getRapidId());
        if (optionalTournament.isEmpty()) return null;

        Tournament tournament = optionalTournament.get();

        Player mostTitlePlayer = null;
        if (dto.getMostTitlePlayerRapidId().isPresent()) {
            mostTitlePlayer = playerService.getOrCreatePlayerByRapidId(dto.getMostTitlePlayerRapidId().get());
        }

        Player titleHolder = null;
        if (dto.getTitleHolder() != null && dto.getTitleHolder().getRapidId() != null) {
            titleHolder = playerService.getOrCreatePlayerByRapidId(dto.getTitleHolder().getRapidId());
        }
        tournament.updatePlayers(mostTitlePlayer, titleHolder);

        return tournament;
    }
}
