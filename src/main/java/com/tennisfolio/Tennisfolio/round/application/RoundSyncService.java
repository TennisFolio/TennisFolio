package com.tennisfolio.Tennisfolio.round.application;

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

    private final StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate;
    private final SeasonRepository seasonRepository;
    private final RoundRepository roundRepository;

    public RoundSyncService(StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate, SeasonRepository seasonRepository, RoundRepository roundRepository) {
        this.leagueRoundsTemplate = leagueRoundsTemplate;
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
                        List<Round> rounds = leagueRoundsTemplate.execute(tournamentRapidId, seasonRapidId);
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
