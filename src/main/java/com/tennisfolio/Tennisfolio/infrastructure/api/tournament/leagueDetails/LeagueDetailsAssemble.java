package com.tennisfolio.Tennisfolio.infrastructure.api.tournament.leagueDetails;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
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
                .orElse(Tournament.builder().rapidTournamentId(dto.getRapidId()).build());

        Player mostTitlePlayer = null;
        if (dto.getMostTitlePlayerRapidId().isPresent()) {
            mostTitlePlayer = Player.builder().rapidPlayerId(dto.getMostTitlePlayerRapidId().get()).build();

        }

        Player titleHolder = null;
        if (dto.getTitleHolder() != null && dto.getTitleHolder().getRapidId() != null) {
            titleHolder = Player.builder().rapidPlayerId(dto.getTitleHolder().getRapidId()).build();

        }
        tournament.updateFromLeagueDetails(mostTitlePlayer, titleHolder, dto.getMostTitles(), dto.getPoints(), dto.getStartTimestamp(), dto.getEndTimestamp());

        return tournament;
    }
}
