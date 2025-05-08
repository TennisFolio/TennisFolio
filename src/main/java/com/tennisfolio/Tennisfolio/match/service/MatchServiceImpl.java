package com.tennisfolio.Tennisfolio.match.service;

import com.tennisfolio.Tennisfolio.api.base.AbstractApiTemplate;
import com.tennisfolio.Tennisfolio.api.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsTemplate;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.response.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.player.service.PlayerService;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MatchServiceImpl implements MatchService{

    private final AbstractApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate;
    private final PlayerService playerService;
    private final AbstractApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundTemplate;
    private final RoundRepository roundRepository;

    public MatchServiceImpl(AbstractApiTemplate<List<LiveEventsApiDTO>, Void> liveEventsTemplate, PlayerService playerService, AbstractApiTemplate<List<LeagueEventsByRoundDTO>, List<Match>> leagueEventsByRoundTemplate, RoundRepository roundRepository){
        this.liveEventsTemplate = liveEventsTemplate;
        this.playerService = playerService;
        this.leagueEventsByRoundTemplate = leagueEventsByRoundTemplate;
        this.roundRepository = roundRepository;
    }
    @Override
    public List<LiveMatchResponse> getLiveEvents() {
        List<LiveEventsApiDTO> apiDTO = liveEventsTemplate.executeWithoutSave("");
        return apiDTO.stream().map(dto -> {
            Player homePlayer = playerService.getOrCreatePlayerByRapidId(dto.getHomeTeam().getRapidPlayerId());
            Player awayPlayer = playerService.getOrCreatePlayerByRapidId(dto.getAwayTeam().getRapidPlayerId());
            return new LiveMatchResponse(dto, homePlayer, awayPlayer);
        }).collect(Collectors.toList());

    }

    @Override
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
