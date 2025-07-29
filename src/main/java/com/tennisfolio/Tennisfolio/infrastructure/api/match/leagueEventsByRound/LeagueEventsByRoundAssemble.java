package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.player.application.PlayerService;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueEventsByRoundAssemble implements EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> {
    private final RoundJpaRepository roundJpaRepository;
    private final SeasonJpaRepository seasonJpaRepository;
    private final PlayerService playerService;
    public LeagueEventsByRoundAssemble(RoundJpaRepository roundJpaRepository, SeasonJpaRepository seasonJpaRepository, PlayerService playerService) {
        this.roundJpaRepository = roundJpaRepository;
        this.seasonJpaRepository = seasonJpaRepository;
        this.playerService = playerService;
    }


    @Override
    public List<Match> assemble(List<LeagueEventsByRoundDTO> dto, Object... params) {
        if(params.length == 0 || params[1] == null || params[2] == null || params[3] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        String rapidSeasonId = params[1].toString();
        Long roundType = Long.parseLong(params[2].toString());
        String slug = params[3].toString();

        SeasonEntity seasonEntity = seasonJpaRepository.findByRapidSeasonId(rapidSeasonId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        Round round = roundJpaRepository.findBySeasonEntityAndRoundAndSlug(seasonEntity,roundType,slug)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND))
                .toModel();

        return dto.stream().map( events -> {

            events.nullToZero();
            events.convertTime();

            Player homePlayer = playerService.getOrCreatePlayerByRapidId(events.getHomeTeam().getRapidPlayerId());
            Player awayPlayer = playerService.getOrCreatePlayerByRapidId(events.getAwayTeam().getRapidPlayerId());

            return new Match(events, round, homePlayer, awayPlayer);
        }).collect(Collectors.toList());



    }
}
