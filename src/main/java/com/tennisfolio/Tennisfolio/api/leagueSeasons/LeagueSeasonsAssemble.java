package com.tennisfolio.Tennisfolio.api.leagueSeasons;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class LeagueSeasonsAssemble implements EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> {
    private final TournamentRepository tournamentRepository;

    public LeagueSeasonsAssemble(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Season> assemble(List<LeagueSeasonsDTO> dto, Object... params) {
        if(params.length == 0 || params[0] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        String rapidTournamentId = params[0].toString();
        Tournament tournament = tournamentRepository.findByRapidTournamentId(rapidTournamentId).get();

        return dto.stream()
                .map(season -> new Season(season,tournament))
                .collect(Collectors.toList());
    }
}
