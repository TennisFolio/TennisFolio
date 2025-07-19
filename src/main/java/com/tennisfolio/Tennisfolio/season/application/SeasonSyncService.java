package com.tennisfolio.Tennisfolio.season.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonSyncService {
    private final StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsTemplate;
    private final StrategyApiTemplate<LeagueSeasonInfoDTO, Season> leagueSeasonInfoTemplate;
    private final TournamentJpaRepository tournamentJpaRepository;
    private final SeasonRepository seasonRepository;

    public SeasonSyncService( StrategyApiTemplate<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsTemplate
            ,  StrategyApiTemplate<LeagueSeasonInfoDTO, Season> leagueSeasonInfoTemplate
            , TournamentJpaRepository tournamentJpaRepository
            , SeasonRepository seasonRepository) {
        this.leagueSeasonsTemplate = leagueSeasonsTemplate;
        this.leagueSeasonInfoTemplate = leagueSeasonInfoTemplate;
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.seasonRepository = seasonRepository;
    }


    public List<Season> saveSeasonList() {
        return tournamentJpaRepository.findAll().stream()
                .map(Tournament::getRapidTournamentId)
                .flatMap(rapidId -> leagueSeasonsTemplate.execute(rapidId).stream())
                .collect(Collectors.toList());

    }

    public List<Season> saveSeasonInfo() {
        return seasonRepository.findAll().stream().map(
                    season -> {
                        String tournamentRapidId = season.getTournament().getRapidTournamentId();
                        String seasonRapidId = season.getRapidSeasonId();
                        return leagueSeasonInfoTemplate.execute(tournamentRapidId, seasonRapidId);
                    }
                ).collect(Collectors.toList());
    }
}
