package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MatchSyncService {

    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final ApiWorker apiWorker;

    public MatchSyncService(RoundRepository roundRepository, MatchRepository matchRepository, ApiWorker apiWorker) {
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
        this.apiWorker = apiWorker;
    }

    public void saveMatchList() {
        Set<String> existingKeys = matchRepository.findAllRapidIds();

        roundRepository.findAll()
                .stream()
                .forEach(round -> {
                    try{
                        String rapidTournamentId = round.getSeason().getTournament().getRapidTournamentId();
                        String rapidSeasonId = round.getSeason().getRapidSeasonId();
                        Long roundNum = round.getRound();
                        if(round.getSlug() == null) return;
                        String slug = round.getSlug();

                        List<Match> matches =  apiWorker.process(RapidApi.LEAGUEEVENETBYROUND,rapidTournamentId, rapidSeasonId, roundNum, slug);
                        List<Match> newMatches = matches.stream()
                                .filter(match -> !existingKeys.contains(match.getRapidMatchId()))
                                .toList();

                        matchRepository.collect(newMatches);
                        matchRepository.flushWhenFull();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                });

        matchRepository.flushAll();


    }
}
