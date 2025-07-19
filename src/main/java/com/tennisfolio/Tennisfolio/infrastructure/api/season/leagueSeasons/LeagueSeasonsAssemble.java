package com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class LeagueSeasonsAssemble implements EntityAssemble<List<LeagueSeasonsDTO>, List<Season>> {
    private final TournamentJpaRepository tournamentJpaRepository;

    public LeagueSeasonsAssemble(TournamentJpaRepository tournamentJpaRepository) {
        this.tournamentJpaRepository = tournamentJpaRepository;
    }

    @Override
    public List<Season> assemble(List<LeagueSeasonsDTO> dto, Object... params) {
        if(params.length == 0 || params[0] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        String rapidTournamentId = params[0].toString();
        Tournament tournament = tournamentJpaRepository.findByRapidTournamentId(rapidTournamentId)
                .orElseThrow(() -> new IllegalArgumentException("조회되는 데이터가 없습니다."));

        return dto.stream()
                .map(season -> new Season(season,tournament))
                .collect(Collectors.toList());
    }
}
