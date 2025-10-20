package com.tennisfolio.Tennisfolio.infrastructure.api.match.eventSchedules;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.EntityAssemble;
import com.tennisfolio.Tennisfolio.infrastructure.api.category.categories.CategoryDTO;
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


    @Override
    public List<Match> assemble(List<EventSchedulesDTO> dto, Object... params) {
        if(params.length == 0 || params[0] == null || params[1] == null || params[2] == null){
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        List<Match> matches = dto.stream()
                .filter(events -> findCategory(events).isSupportedCategory())
                .map( events-> {

            Category category = findCategory(events);

            Tournament tournament = findTournament(events, category);

            Season season = findSeason(events, tournament);

            Round round = findRound(events, season);

            Player homePlayer = Player.builder().rapidPlayerId(events.getHomeTeam().getRapidPlayerId()).build();
            Player awayPlayer = Player.builder().rapidPlayerId(events.getAwayTeam().getRapidPlayerId()).build();

            Score homeScore = scoreBuilder(events.getHomeScore());

            Score awayScore = scoreBuilder(events.getAwayScore());

            Period periodSet = periodBuilder(events.getTime());

            String status = events.getStatus().getDescription();

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
                    .status(status)
                    .startTimestamp(events.getStartTimestamp())
                    .build();
        }).collect(Collectors.toList());

        return matches.stream().map(p -> {
            p.nullToZero();
            p.convertTime();
            return p;
        }).toList();

    }

    private Category findCategory(EventSchedulesDTO events){
        CategoryDTO category = events.getTournament().getCategory();

        return Category.builder()
                .rapidCategoryId(category.getRapidId())
                .categorySlug(category.getSlug())
                .categoryName(category.getName())
                .build();
    }

    private Tournament findTournament(EventSchedulesDTO events, Category category){

        return Tournament.builder()
                        .rapidTournamentId(events.getTournament().getRapidId())
                        .tournamentName(events.getTournament().getName())
                        .category(category)
                        .build();
    }

    private Season findSeason(EventSchedulesDTO events, Tournament tournament){

        return Season.builder()
                .rapidSeasonId(events.getSeason().getRapidId())
                .seasonName(events.getSeason().getName())
                .year(events.getSeason().getYear())
                .tournament(tournament).build();
    }

    private Round findRound(EventSchedulesDTO events, Season season){

        return Round.builder()
                .season(season)
                .round(events.getRound().getRound())
                .name(events.getRound().getName())
                .slug(events.getRound().getSlug())
                .build();
    }

    private Score scoreBuilder(ScoreDTO scoreDTO){
        return Score.builder()
                .set1(scoreDTO.getPeriod1())
                .set2(scoreDTO.getPeriod2())
                .set3(scoreDTO.getPeriod3())
                .set4(scoreDTO.getPeriod4())
                .set5(scoreDTO.getPeriod5())
                .set1Tie(scoreDTO.getPeriod1TieBreak())
                .set2Tie(scoreDTO.getPeriod2TieBreak())
                .set3Tie(scoreDTO.getPeriod3TieBreak())
                .set4Tie(scoreDTO.getPeriod4TieBreak())
                .set5Tie(scoreDTO.getPeriod5TieBreak())
                .build();
    }

    private Period periodBuilder(TimeDTO timeSet){
        return Period.builder()
                .set1(timeSet.getPeriod1())
                .set2(timeSet.getPeriod2())
                .set3(timeSet.getPeriod3())
                .set4(timeSet.getPeriod4())
                .set5(timeSet.getPeriod5())
                .build();
    }
}
