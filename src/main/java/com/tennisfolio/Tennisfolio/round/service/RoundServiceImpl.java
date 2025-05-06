package com.tennisfolio.Tennisfolio.round.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoundServiceImpl implements RoundService{

    private final AbstractApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate;
    private final SeasonRepository seasonRepository;

    public RoundServiceImpl(AbstractApiTemplate<List<LeagueRoundsDTO>, List<Round>> leagueRoundsTemplate, SeasonRepository seasonRepository) {
        this.leagueRoundsTemplate = leagueRoundsTemplate;
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<Round> saveRoundList() {
        return seasonRepository.findAll()
                .stream()
                .map(season -> {
                    String tournamentRapidId = season.getTournament().getRapidTournamentId();
                    String seasonRapidId = season.getRapidSeasonId();
                    return leagueRoundsTemplate.execute(tournamentRapidId, seasonRapidId);
                })
                .flatMap(list -> list != null ? list.stream() : Stream.empty())
                .collect(Collectors.toList());


    }
}
