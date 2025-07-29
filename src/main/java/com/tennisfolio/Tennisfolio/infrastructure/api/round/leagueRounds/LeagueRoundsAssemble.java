package com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueRoundsAssemble implements EntityAssemble<List<LeagueRoundsDTO>, List<Round>> {
    private final SeasonRepository seasonRepository;

    public LeagueRoundsAssemble(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Round> assemble(List<LeagueRoundsDTO> dto, Object... params) {
        if(params.length == 0 || params[0] == null || params[1] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        String seasonRapidId = params[1].toString();
        Season season = seasonRepository.findByRapidSeasonId(seasonRapidId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND))
                .toModel();

        return dto.stream()
                .map(round -> new Round(round, season))
                .collect(Collectors.toList());
    }
}
