package com.tennisfolio.Tennisfolio.round.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import org.apache.commons.lang3.tuple.Pair;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoundSyncService {

    private final ApiWorker apiWorker;
    private final SeasonRepository seasonRepository;
    private final RoundRepository roundRepository;

    public RoundSyncService(ApiWorker apiWorker, SeasonRepository seasonRepository, RoundRepository roundRepository) {
        this.apiWorker = apiWorker;
        this.seasonRepository = seasonRepository;
        this.roundRepository = roundRepository;
    }

    public void saveRoundList() {

        Set<Pair<Season, String>> existingKeys = roundRepository.findAllSeasonRoundPairs();

        seasonRepository.findAll()
                .stream()
                .forEach(season -> {
                    try{
                        String tournamentRapidId = season.getTournament().getRapidTournamentId();
                        String seasonRapidId = season.getRapidSeasonId();
                        List<Round> rounds = apiWorker.process(RapidApi.LEAGUEROUNDS, tournamentRapidId, seasonRapidId);
                        List<Round> newRounds = rounds
                                .stream()
                                .filter(round -> !existingKeys.contains(Pair.of(round.getSeason(), round.getRound())))
                                .toList();

                        roundRepository.collect(newRounds);
                        roundRepository.flushWhenFull();
                    }catch(Exception e){
                        e.printStackTrace();
                        System.out.println(season.getRapidSeasonId());
                    }
                });

        roundRepository.flushAll();
    }
}
