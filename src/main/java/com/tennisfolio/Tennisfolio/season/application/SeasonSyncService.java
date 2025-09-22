package com.tennisfolio.Tennisfolio.season.application;

import com.tennisfolio.Tennisfolio.Tournament.application.TournamentQueryService;
import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.application.CategoryService;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasons.LeagueSeasonsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.season.leagueSeasonInfo.LeagueSeasonInfoDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeasonSyncService {
    private final ApiWorker apiWorker;
    private final TournamentQueryService tournamentQueryService;
    private final SeasonRepository seasonRepository;

    public SeasonSyncService(ApiWorker apiWorker
            , TournamentQueryService tournamentQueryService
            , SeasonRepository seasonRepository
    ) {
        this.apiWorker = apiWorker;
        this.tournamentQueryService = tournamentQueryService;
        this.seasonRepository = seasonRepository;
    }


    public void saveSeasonList() {

        Set<String> existingKeys = seasonRepository.findAllRapidIds();
        List<String> failedRapidIds = new ArrayList<>();

        tournamentQueryService.getAllTournament().stream()
                .filter(tournament -> tournament.getCategory().isSupportedCategory())
                .map(Tournament::getRapidTournamentId)
                .forEach(tournamentRapidId -> {
                    try{
                        List<Season> seasonList = apiWorker.process(RapidApi.LEAGUESEASONS, tournamentRapidId);
                        List<Season> newSeasons = seasonList.stream()
                                .filter(season -> !existingKeys.contains(season.getRapidSeasonId()))
                                .filter(Season::isSince2019)
                                .toList();
                        seasonRepository.collect(newSeasons);
                        seasonRepository.flushWhenFull();
                    }catch(Exception e){
                        e.printStackTrace();
                        failedRapidIds.add(tournamentRapidId);
                    }
                });
        seasonRepository.flushAll();

        System.out.println("failedRapidIds: " + failedRapidIds);


    }

    public void saveSeasonInfo() {
        List<Season> findSeasons = seasonRepository.findAll();
        findSeasons.stream().forEach(
                    season -> {
                        try{
                            String tournamentRapidId = season.getTournament().getRapidTournamentId();
                            String seasonRapidId = season.getRapidSeasonId();
                            Season leagueSeasonInfo = apiWorker.process(RapidApi.LEAGUESEASONINFO, tournamentRapidId, seasonRapidId);
                            seasonRepository.collect(leagueSeasonInfo);
                            seasonRepository.flushWhenFull();

                        }catch(Exception e){
                            e.printStackTrace();
                            System.out.println(season.getRapidSeasonId());
                        }
                    });
        seasonRepository.flushAll();
    }
}
