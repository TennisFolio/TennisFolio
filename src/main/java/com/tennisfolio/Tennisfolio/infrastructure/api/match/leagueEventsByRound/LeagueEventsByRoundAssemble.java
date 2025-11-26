package com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.liveEvents.*;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.domain.Period;
import com.tennisfolio.Tennisfolio.match.domain.Score;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeagueEventsByRoundAssemble implements EntityAssemble<List<LeagueEventsByRoundDTO>, List<Match>> {


    @Override
    public List<Match> assemble(List<LeagueEventsByRoundDTO> dto, Object... params) {
        if(params.length == 0 || params[1] == null || params[2] == null || params[3] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        List<Match> matches = dto.stream().map( events -> {

            SeasonDTO seasonDTO = events.getSeason();
            Season season = findSeason(seasonDTO);
            // Round
            RoundDTO roundDTO = events.getRound();
            Round round = findRound(roundDTO, season);

            // homePlayer
            TeamDTO homePlayerDTO = events.getHomeTeam();
            Player homePlayer = findPlayer(homePlayerDTO);

            // awayPlayer
            TeamDTO awayPlayerDTO = events.getAwayTeam();
            Player awayPlayer = findPlayer(awayPlayerDTO);

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
                    .rapidMatchId(events.getRapidMatchId())
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
                    .startTimestamp(events.getStartTimestamp())
                    .winner(events.getWinner())
                    .status(events.getStatus().getDescription())
                    .build();

        }).collect(Collectors.toList());

        return matches.stream().map(p -> {
            p.nullToZero();
            p.convertTime();
            return p;
        }).toList();


    }


    private Player findPlayer(TeamDTO teamDTO){
        return Player.builder()
                .rapidPlayerId(teamDTO.getRapidPlayerId())
                .playerName(teamDTO.getName())
                .build();
    }

    private Round findRound(RoundDTO roundDTO, Season season){
        return Round.builder()
                .season(season)
                .round(roundDTO.getRound())
                .name(roundDTO.getName())
                .slug(roundDTO.getSlug())
                .build();
    }

    private Season findSeason(SeasonDTO seasonDTO){
        return Season.builder()
                .rapidSeasonId(seasonDTO.getRapidId())
                .seasonName(seasonDTO.getName())
                .year(seasonDTO.getYear())
                .build();
    }
}
