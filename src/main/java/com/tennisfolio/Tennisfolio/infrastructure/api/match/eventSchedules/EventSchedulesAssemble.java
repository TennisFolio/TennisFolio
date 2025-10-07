package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.ScoreDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.TimeDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventSchedulesAssemble implements EntityAssemble<List<EventSchedulesDTO>, List<Match>> {
    private final TournamentRepository tournamentRepository;
    private final SeasonRepository seasonRepository;
    private final RoundRepository roundRepository;

    public EventSchedulesAssemble(TournamentRepository tournamentRepository, SeasonRepository seasonRepository, RoundRepository roundRepository) {
        this.tournamentRepository = tournamentRepository;
        this.seasonRepository = seasonRepository;
        this.roundRepository = roundRepository;
    }

    @Override
    public List<Match> assemble(List<EventSchedulesDTO> dto, Object... params) {
        if(params.length == 0 || params[1] == null || params[2] == null || params[3] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        List<Match> matches = dto.stream().map( events-> {
            Tournament tournament = tournamentRepository.findByRapidTournamentId(events.getTournament().getRapidId())
                    .orElse(Tournament.builder()
                            .rapidTournamentId(events.getRapidId())
                            .build());

            Season season = seasonRepository.findByRapidSeasonId(events.getSeason().getRapidId())
                    .orElse(Season.builder()
                            .rapidSeasonId(events.getRapidId())
                            .tournament(tournament).build());

            Round round = roundRepository.findBySeasonAndRoundAndSlug(season,events.getRound().getRound(),events.getRound().getSlug())
                    .orElse(Round.builder()
                            .season(season)
                            .round(events.getRound().getRound())
                            .slug(events.getRound().getSlug())
                            .build());

            Player homePlayer = Player.builder().rapidPlayerId(events.getHomeTeam().getRapidPlayerId()).build();
            Player awayPlayer = Player.builder().rapidPlayerId(events.getAwayTeam().getRapidPlayerId()).build();

            ScoreDTO homeScoreDTO = events.getHomeScore();
            Score homeScore = Score.builder()
                    .set1(homeScoreDTO.getPeriod1())
                    .set2(homeScoreDTO.getPeriod2())
                    .set3(homeScoreDTO.getPeriod3())
                    .set4(homeScoreDTO.getPeriod4())
                    .set5(homeScoreDTO.getPeriod5())
                    .set1Tie(homeScoreDTO.getPeriod1TieBreak())
                    .set2Tie(homeScoreDTO.getPeriod2TieBreak())
                    .set3Tie(homeScoreDTO.getPeriod3TieBreak())
                    .set4Tie(homeScoreDTO.getPeriod4TieBreak())
                    .set5Tie(homeScoreDTO.getPeriod5TieBreak())
                    .build();

            ScoreDTO awayScoreDTO = events.getAwayScore();
            Score awayScore = Score.builder()
                    .set1(awayScoreDTO.getPeriod1())
                    .set2(awayScoreDTO.getPeriod2())
                    .set3(awayScoreDTO.getPeriod3())
                    .set4(awayScoreDTO.getPeriod4())
                    .set5(awayScoreDTO.getPeriod5())
                    .set1Tie(awayScoreDTO.getPeriod1TieBreak())
                    .set2Tie(awayScoreDTO.getPeriod2TieBreak())
                    .set3Tie(awayScoreDTO.getPeriod3TieBreak())
                    .set4Tie(awayScoreDTO.getPeriod4TieBreak())
                    .set5Tie(awayScoreDTO.getPeriod5TieBreak())
                    .build();
            TimeDTO timeSet = events.getTime();
            Period periodSet = Period.builder()
                    .set1(timeSet.getPeriod1())
                    .set2(timeSet.getPeriod2())
                    .set3(timeSet.getPeriod3())
                    .set4(timeSet.getPeriod4())
                    .set5(timeSet.getPeriod5())
                    .build();

            return Match.builder()
                    .rapidMatchId(events.getRapidId())
                    .homeSeed(events.getHomeTeamSeed())
                    .awaySeed(events.getAwayTeamSeed())
                    .homeScore(events.getHomeScore().getCurrent())
                    .awayScore(events.getAwayScore().getCurrent())
                    .round(round)
                    .homePlayer(homePlayer)
                    .awayPlayer(awayPlayer)
                    .homeSet(homeScore)
                    .awaySet(awayScore)
                    .periodSet(periodSet)
                    .startTimeStamp(events.getStartTimestamp())
                    .build();
        }).collect(Collectors.toList());

        return matches.stream().map(p -> {
            p.nullToZero();
            p.convertTime();
            return p;
        }).toList();



    }
}
