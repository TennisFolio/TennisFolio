package com.tennisfolio.Tennisfolio.round.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoundSyncService {

    private final StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate;
    private final SeasonJpaRepository seasonJpaRepository;

    public RoundSyncService(StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate, SeasonJpaRepository seasonJpaRepository) {
        this.leagueRoundsTemplate = leagueRoundsTemplate;
        this.seasonJpaRepository = seasonJpaRepository;
    }

    public List<Round> saveRoundList() {
        return seasonJpaRepository.findAll()
                .stream()
                .map(season -> {
                    String tournamentRapidId = season.getTournamentEntity().getRapidTournamentId();
                    String seasonRapidId = season.getRapidSeasonId();
                    return leagueRoundsTemplate.execute(tournamentRapidId, seasonRapidId);
                })
                .flatMap(list -> list != null ? list.stream() : Stream.empty())
                .collect(Collectors.toList());


    }
}
