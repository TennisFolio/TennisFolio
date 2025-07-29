package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MatchSyncService {

    private final RoundRepository roundRepository;
    private final StrategyApiTemplate<List<LeagueEventsByRoundDTO>,List<Match>> leagueEventsByRoundTemplate;

    public MatchSyncService(RoundRepository roundRepository, StrategyApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundTemplate) {
        this.roundRepository = roundRepository;
        this.leagueEventsByRoundTemplate = leagueEventsByRoundTemplate;
    }

    public List<Match> saveMatchList() {
        return roundRepository.findAll()
                .stream()
                .map(round -> {
                    String rapidTournamentId = round.getSeason().getTournament().getRapidTournamentId();
                    String rapidSeasonId = round.getSeason().getRapidSeasonId();
                    Long roundNum = round.getRound();
                    String slug = round.getSlug();

                    return leagueEventsByRoundTemplate.execute(rapidTournamentId, rapidSeasonId, roundNum, slug);
                }).flatMap(list -> list != null ? list.stream() : Stream.empty())
                .collect(Collectors.toList());

    }
}
