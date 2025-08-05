package com.tennisfolio.Tennisfolio.round.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundEntity;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoundSyncService {

    private final StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate;
    private final SeasonRepository seasonRepository;

    public RoundSyncService(StrategyApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate, SeasonRepository seasonRepository) {
        this.leagueRoundsTemplate = leagueRoundsTemplate;
        this.seasonRepository = seasonRepository;
    }

    public List<Round> saveRoundList() {
        return seasonRepository.findAll()
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
