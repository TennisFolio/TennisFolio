package com.tennisfolio.Tennisfolio.api.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueEventsByRoundAssemble implements EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> {
    private final RoundRepository roundRepository;
    private final SeasonRepository seasonRepository;
    private final PlayerService playerService;
    public LeagueEventsByRoundAssemble(RoundRepository roundRepository, SeasonRepository seasonRepository, PlayerService playerService) {
        this.roundRepository = roundRepository;
        this.seasonRepository = seasonRepository;
        this.playerService = playerService;
    }


    @Override
    public List<Match> assemble(List<LeagueEventsByRoundDTO> dto, Object... params) {
        if(params.length == 0 || params[1] == null || params[2] == null || params[3] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        String rapidSeasonId = params[1].toString();
        Long round = Long.parseLong(params[2].toString());
        String slug = params[3].toString();

        Season findSeason = seasonRepository.findByRapidSeasonId(rapidSeasonId).get();
        Round findRound = roundRepository.findBySeasonAndRoundAndSlug(findSeason,round,slug).get();

        return dto.stream().map( events -> {

            events.nullToZero();
            events.convertTime();

            Player homePlayer = playerService.getOrCreatePlayerByRapidId(events.getHomeTeam().getRapidPlayerId());
            Player awayPlayer = playerService.getOrCreatePlayerByRapidId(events.getAwayTeam().getRapidPlayerId());

            return new Match(events, findRound, homePlayer, awayPlayer);
        }).collect(Collectors.toList());



    }
}
