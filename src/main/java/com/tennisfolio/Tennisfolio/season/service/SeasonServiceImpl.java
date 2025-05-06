package com.tennisfolio.Tennisfolio.season.service;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.api.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService{
    private final AbstractApiTemplate<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsTemplate;
    private final AbstractApiTemplate<LeagueSeasonInfoDTO, Season> leagueSeasonInfoTemplate;
    private final TournamentRepository tournamentRepository;
    private final SeasonRepository seasonRepository;

    public SeasonServiceImpl(@Qualifier("leagueSeasonsTemplate") AbstractApiTemplate<List<LeagueSeasonsDTO>, List<Season>> leagueSeasonsTemplate
            , @Qualifier("leagueSeasonInfoTemplate") AbstractApiTemplate<LeagueSeasonInfoDTO, Season> leagueSeasonInfoTemplate
            , TournamentRepository tournamentRepository
            , SeasonRepository seasonRepository) {
        this.leagueSeasonsTemplate = leagueSeasonsTemplate;
        this.leagueSeasonInfoTemplate = leagueSeasonInfoTemplate;
        this.tournamentRepository = tournamentRepository;
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Season> saveSeasonList() {
        return tournamentRepository.findAll().stream()
                .map(Tournament::getRapidTournamentId)
                .flatMap(rapidId -> leagueSeasonsTemplate.execute(rapidId).stream())
                .collect(Collectors.toList());

    }

    @Override
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
