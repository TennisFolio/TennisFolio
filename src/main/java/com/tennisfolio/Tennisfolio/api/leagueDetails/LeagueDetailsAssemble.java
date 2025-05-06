package com.tennisfolio.Tennisfolio.api.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
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
    public Tournament assemble(LeagueDetailsDTO dto) {
        return tournamentRepository.findByRapidTournamentId(dto.getRapidId())
                .map(
                        tournament -> {
                            if (dto.getTitleHolder() != null) {
                                tournament.setTitleHolder(
                                        playerService.getOrCreatePlayerByRapidId(dto.getTitleHolder().getRapidId())
                                );
                            }

                            dto.getMostTitlePlayerRapidId()
                                    .map(playerService::getOrCreatePlayerByRapidId)
                                    .ifPresent(tournament::setMostTitlePlayer);
                            tournament.setPoints(dto.getPoints());

                            return tournament;
                        }
                ).orElse(null);
    }
}
