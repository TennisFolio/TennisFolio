package com.tennisfolio.Tennisfolio.match.application;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryRepository;
import com.tennisfolio.Tennisfolio.common.RoundType;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.match.leagueEventsByRound.LeagueEventsByRoundDTO;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.infrastructure.repository.RoundJpaRepository;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.infrastructure.PlayerProvider;
import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MatchSyncService {

    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final SeasonRepository seasonRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final ApiWorker apiWorker;
    private final PlayerProvider playerProvider;
    private final Clock clock;

    public MatchSyncService(CategoryRepository categoryRepository, TournamentRepository tournamentRepository, SeasonRepository seasonRepository, RoundRepository roundRepository, MatchRepository matchRepository, ApiWorker apiWorker, PlayerProvider playerProvider, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.seasonRepository = seasonRepository;
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
        this.apiWorker = apiWorker;
        this.playerProvider = playerProvider;
        this.clock = clock;
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

    @Transactional
    public void saveEventSchedule(){
        List<Match> allEvents = new ArrayList<>();

        fetchUpcomingMatchSchedules(allEvents);

        allEvents.stream()
                .filter(match -> match.getTournament().getCategory().isSupportedCategory())
                .forEach(match -> {
                    Optional<Match> existingOpt = matchRepository.findByRapidMatchId(match.getRapidMatchId());

                    // 존재하는 경우 → dirty checking (UPDATE)
                    if (existingOpt.isPresent()) {
                        Match existing = existingOpt.get(); // 영속 상태
                        existing.updateFrom(match);         // 필드 변경
                        matchRepository.save(existing);
                        // dirty checking 자동으로 감지됨 (save() 필요 X)
                    }
                    // 존재하지 않는 경우 → 새로 저장 (INSERT)
                    else {
                        Category category = findOrSaveCategory(match);
                        Tournament tournament = findOrSaveTournament(match, category);
                        Season season = findOrSaveSeason(match, tournament);
                        Round round = findOrSaveRound(match, season);
                        match.updateRound(round);

                        Player homePlayer = playerProvider.provide(match.getHomePlayer().getRapidPlayerId());
                        Player awayPlayer = playerProvider.provide(match.getAwayPlayer().getRapidPlayerId());
                        roundRepository.flush();
                        match.updatePlayer(homePlayer, awayPlayer);

                        matchRepository.save(match); // INSERT
                    }
                });

    }

    private void fetchUpcomingMatchSchedules(List<Match> allEvents) {
        for (int i = 0; i <= 2; i++) {
            LocalDate date = LocalDate.now(clock).plusDays(i);
            String year = String.valueOf(date.getYear());
            String month = String.format("%02d", date.getMonthValue());
            String day = String.format("%02d", date.getDayOfMonth());

            List<Match> events = apiWorker.process(RapidApi.EVENTSCHEDULES, day, month, year);
            allEvents.addAll(events);
        }
    }

    private Category findOrSaveCategory(Match match){
        return categoryRepository.findByRapidCategoryId(match.getCategory().getRapidCategoryId())
                .orElseGet(() -> categoryRepository.save(match.getCategory()));
    }

    private Tournament findOrSaveTournament(Match match, Category category){
        String rapidTournamentId = match.getTournament().getRapidTournamentId();
        Tournament tournament =  tournamentRepository.findByRapidTournamentId(rapidTournamentId)
                .map(existing -> {
                    existing.updateCategory(category);
                    if(existing.needsTournamentInfo()){
                        requestTournamentInfo(existing, rapidTournamentId);
                    }
                    if(existing.needsLeagueDetails()){
                        requestLeagueDetails(existing, rapidTournamentId);
                    }

                    return tournamentRepository.save(existing);
                })
                .orElseGet(() -> {
                    Tournament newTournament = match.getTournament();

                    requestTournamentInfo(newTournament, rapidTournamentId);

                    requestLeagueDetails(newTournament, rapidTournamentId);

                    newTournament.updateCategory(category);

                    Tournament savedTournament = tournamentRepository.save(newTournament);
                    savedTournament.updateTimestamp(newTournament.getStartTimestamp(), newTournament.getEndTimestamp());
                    return savedTournament;

                });

        return tournament;
    }

    private void requestTournamentInfo(Tournament newTournament, String rapidTournamentId){
        Tournament tournamentInfo = apiWorker.process(RapidApi.TOURNAMENTINFO, rapidTournamentId);
        if(tournamentInfo != null){
            newTournament.updateFromTournamentInfo(tournamentInfo.getCity(), tournamentInfo.getMatchType(), tournamentInfo.getGroundType());
        }
    }

    private void requestLeagueDetails(Tournament newTournament, String rapidTournamentId){
        Tournament leagueDetails = apiWorker.process(RapidApi.LEAGUEDETAILS, rapidTournamentId);
        Player mostTitleHolder = null;
        Player titleHolder = null;
        if(leagueDetails != null){
            if(leagueDetails.isMostTitlePlayerExists()){
                mostTitleHolder = playerProvider.provide(leagueDetails.getMostTitlePlayer().getRapidPlayerId());
            }
            if(leagueDetails.isTitleHolderExists()){
                titleHolder = playerProvider.provide(leagueDetails.getTitleHolder().getRapidPlayerId());
            }
            newTournament.updateFromLeagueDetails(mostTitleHolder, titleHolder,
                    leagueDetails.getMostTitles(), leagueDetails.getPoints(),
                    leagueDetails.getStartTimestamp(), leagueDetails.getEndTimestamp());
        }
    }

    private Season findOrSaveSeason(Match match, Tournament tournament){
        String rapidTournamentId = tournament.getRapidTournamentId();

        Season season = seasonRepository.findByRapidSeasonId(match.getSeason().getRapidSeasonId())
                .map(existing -> {
                    existing.updateTournament(tournament);
                    requestLeagueDetails(tournament, rapidTournamentId);
                    existing.updateTimestamp();

                    if(existing.needsLeagueSeasonInfo()){
                        requestLeagueSeasonInfo(existing, rapidTournamentId);
                    }
                    return seasonRepository.save(existing);
                })
                .orElseGet(() -> {
                    Season newSeason = match.getSeason();
                    newSeason.updateTournament(tournament);

                    requestLeagueSeasonInfo(newSeason, rapidTournamentId);
                    newSeason.updateTimestamp();
                    return seasonRepository.save(newSeason);

                });

        return season;
    }

    private void requestLeagueSeasonInfo(Season newSeason, String rapidTournamentId){
        Season leagueSeasonInfo = apiWorker.process(RapidApi.LEAGUESEASONINFO, rapidTournamentId, newSeason.getRapidSeasonId());
        if(leagueSeasonInfo != null){
            System.out.println(rapidTournamentId + " " + newSeason.getRapidSeasonId());
            newSeason.updateFromLeagueSeasonInfo(leagueSeasonInfo.getTotalPrize(),leagueSeasonInfo.getTotalPrizeCurrency(),leagueSeasonInfo.getCompetitors());
        }

    }

    private Round findOrSaveRound(Match match, Season season){
        return roundRepository.findBySeasonAndRound(season, match.getRound().getRound())
                .map(existing -> {
                    existing.updateSeason(season);
                    return existing;
                })
                .orElseGet(() -> {
                    Round newRound = match.getRound();
                    newRound.updateSeason(season);
                    RoundType roundType = RoundType.fromSlug(newRound.getSlug());
                    newRound.updateRoundInfo(roundType);
                    Round saved = roundRepository.save(newRound);
                    return saved;
                });
    }
}
