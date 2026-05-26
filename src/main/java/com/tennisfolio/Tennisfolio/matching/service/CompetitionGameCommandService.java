package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionStatResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CourtCountUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameBalanceResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CompetitionGameCommandService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;
    private final CompetitionStatRepository competitionStatRepository;
    private final GameRepository gameRepository;
    private final GameEntryRepository gameEntryRepository;
    private final TennisMatchScheduler scheduler;
    private final GameService gameService;
    private final CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    public CompetitionGameCommandService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository,
            CompetitionStatRepository competitionStatRepository,
            GameRepository gameRepository,
            GameEntryRepository gameEntryRepository,
            TennisMatchScheduler scheduler,
            GameService gameService,
            CompetitionAdminAuthorizationService competitionAdminAuthorizationService
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
        this.competitionStatRepository = competitionStatRepository;
        this.gameRepository = gameRepository;
        this.gameEntryRepository = gameEntryRepository;
        this.scheduler = scheduler;
        this.gameService = gameService;
        this.competitionAdminAuthorizationService = competitionAdminAuthorizationService;
    }

    @Transactional
    public GameResponse createNextCourtGame(String publicId, Integer court, String adminToken) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        validateClubSession(competition);
        validateCourt(competition, court);

        List<CompetitionEntry> candidates = competitionEntryRepository
                .findByCompetitionIdAndStatus(competition.getId(), CompetitionEntry.EntryStatus.ACTIVE);

        if (candidates.size() < 4) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        int nextRound = gameRepository.findMaxRoundByCompetitionIdAndCourt(competition.getId(), court) + 1;
        List<GameEntry> history = gameEntryRepository.findScheduleEntriesByCompetitionId(competition.getId());
        GameMatch match = scheduler.generateNextClubSessionGame(
                candidates,
                history,
                court,
                nextRound,
                ThreadLocalRandom.current().nextLong()
        );

        Map<String, CompetitionEntry> entriesById = new HashMap<>();
        for (CompetitionEntry candidate : candidates) {
            entriesById.put(String.valueOf(candidate.getId()), candidate);
        }
        Game game = gameService.saveGame(competition, match, entriesById);
        CompetitionStat stat = recalculateCompetitionStat(competition);

        return GameResponse.from(game, gameEntryRepository.findByGameId(game.getId()));
    }

    @Transactional
    public GameResponse updateGameStatus(
            String publicId,
            Long gameId,
            String adminToken,
            GameStatusUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        validateClubSession(competition);

        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        Game.GameStatus status = resolveGameStatus(request.getStatus());
        if (game.getStatus() != Game.GameStatus.READY || status != Game.GameStatus.COMPLETED) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        game.recordScore(
                normalizeScore(request.getTeamAScore()),
                normalizeScore(request.getTeamBScore()),
                0,
                0
        );
        game.complete();
        return GameResponse.from(game, gameEntryRepository.findByGameId(game.getId()));
    }

    @Transactional
    public void deleteGame(String publicId, Long gameId, String adminToken) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        validateClubSession(competition);

        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        if (game.getStatus() != Game.GameStatus.READY) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        gameEntryRepository.deleteByGameId(game.getId());
        gameRepository.delete(game);
        recalculateCompetitionStat(competition);
    }

    @Transactional
    public CompetitionStatResponse updateCourtCount(
            String publicId,
            String adminToken,
            CourtCountUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        validateClubSession(competition);

        if (request.getCourtCount() == null || request.getCourtCount() <= 0 || request.getCourtCount() > 10) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        relocateReadyGamesOutsideCourtLimit(competition, request.getCourtCount());
        competition.updateCourtCount(request.getCourtCount());
        return CompetitionStatResponse.from(recalculateCompetitionStat(competition));
    }

    @Transactional
    public GameEntryUpdateResponse updateGameEntries(
            String publicId,
            Long gameId,
            String adminToken,
            GameEntryUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);

        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        if (competition.getMode() == Competition.CompetitionMode.CLUB_SESSION) {
            validateReadyGame(game);
        } else {
            validateReadyCompetition(competition);
        }
        List<GameEntry> currentGameEntries = gameEntryRepository.findByGameId(game.getId());
        boolean gameEntriesChanged = hasGameEntriesChanged(currentGameEntries, request);

        List<CompetitionEntry> teamA = resolveTeamEntries(competition, request.getTeamA());
        List<CompetitionEntry> teamB = resolveTeamEntries(competition, request.getTeamB());
        validateDistinctPlayers(teamA, teamB);
        if (competition.getMode() == Competition.CompetitionMode.CLUB_SESSION) {
            validateClubSessionGameEntries(competition, teamA, teamB);
        }

        game.updateMatchType(resolveMatchType(teamA, teamB));
        if (gameEntriesChanged && hasRecordedScore(game)) {
            game.recordScore(0, 0, 0, 0);
        }

        gameEntryRepository.deleteByGameId(game.getId());
        List<GameEntry> savedGameEntries = gameEntryRepository.saveAll(createGameEntries(game, teamA, teamB));
        CompetitionStat stat = recalculateCompetitionStat(competition);

        return new GameEntryUpdateResponse(
                GameResponse.from(game, savedGameEntries),
                CompetitionStatResponse.from(stat),
                GameBalanceResponse.from(stat)
        );
    }

    @Transactional
    public GameResponse updateGameScore(
            String publicId,
            Long gameId,
            String adminToken,
            GameScoreUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competitionAdminAuthorizationService.validateAdminToken(publicId, adminToken);
        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        if (competition.getMode() == Competition.CompetitionMode.CLUB_SESSION) {
            if (game.getStatus() != Game.GameStatus.COMPLETED) {
                throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
            }
        }

        Integer teamAScore = normalizeScore(request.getTeamAScore());
        Integer teamBScore = normalizeScore(request.getTeamBScore());
        Integer teamATiebreakScore = competition.getMode() == Competition.CompetitionMode.CLUB_SESSION
                ? 0
                : normalizeScore(request.getTeamATiebreakScore());
        Integer teamBTiebreakScore = competition.getMode() == Competition.CompetitionMode.CLUB_SESSION
                ? 0
                : normalizeScore(request.getTeamBTiebreakScore());

        game.recordScore(teamAScore, teamBScore, teamATiebreakScore, teamBTiebreakScore);
        return GameResponse.from(game, gameEntryRepository.findByGameId(game.getId()));
    }

    private Integer normalizeScore(Integer score) {
        if (score == null) {
            return 0;
        }
        if (score < 0 || score > 99) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        return score;
    }

    private boolean hasRecordedScore(Game game) {
        return scoreValue(game.getTeamAScore()) > 0
                || scoreValue(game.getTeamBScore()) > 0
                || scoreValue(game.getTeamATiebreaKScore()) > 0
                || scoreValue(game.getTeamBTiebreaKScore()) > 0;
    }

    private int scoreValue(Integer score) {
        return score == null ? 0 : score;
    }

    private boolean hasGameEntriesChanged(List<GameEntry> currentGameEntries, GameEntryUpdateRequest request) {
        return hasTeamEntriesChanged(currentGameEntries, GameEntry.Team.A, request.getTeamA())
                || hasTeamEntriesChanged(currentGameEntries, GameEntry.Team.B, request.getTeamB());
    }

    private boolean hasTeamEntriesChanged(
            List<GameEntry> currentGameEntries,
            GameEntry.Team team,
            List<GameEntryUpdateRequest.PlayerRequest> playerRequests
    ) {
        if (playerRequests == null || playerRequests.size() != 2) {
            return true;
        }

        Map<Integer, Long> currentEntryIdsByPosition = new HashMap<>();
        for (GameEntry gameEntry : currentGameEntries) {
            if (gameEntry.getTeam() == team) {
                currentEntryIdsByPosition.put(
                        gameEntry.getPosition(),
                        gameEntry.getCompetitionEntry().getId()
                );
            }
        }

        for (int index = 0; index < playerRequests.size(); index++) {
            GameEntryUpdateRequest.PlayerRequest playerRequest = playerRequests.get(index);
            if (playerRequest.getCompetitionEntryId() == null) {
                return true;
            }
            Integer position = playerRequest.getPosition() != null ? playerRequest.getPosition() : index + 1;
            if (!playerRequest.getCompetitionEntryId().equals(currentEntryIdsByPosition.get(position))) {
                return true;
            }
        }

        return currentEntryIdsByPosition.size() != playerRequests.size();
    }

    private void validateReadyCompetition(Competition competition) {
        if (competition.getStatus() != Competition.CompetitionStatus.READY) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateReadyGame(Game game) {
        if (game.getStatus() != Game.GameStatus.READY) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateClubSession(Competition competition) {
        if (competition.getMode() != Competition.CompetitionMode.CLUB_SESSION) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateCourt(Competition competition, Integer court) {
        if (court == null || court <= 0 || court > competition.getCourtCount()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void relocateReadyGamesOutsideCourtLimit(Competition competition, int targetCourtCount) {
        if (targetCourtCount >= competition.getCourtCount()) {
            return;
        }

        List<Game> readyGames = gameRepository.findByCompetitionIdAndStatus(
                competition.getId(),
                Game.GameStatus.READY
        );
        Set<Integer> occupiedCourts = new HashSet<>();
        List<Game> gamesToRelocate = new ArrayList<>();

        for (Game game : readyGames) {
            if (game.getCourt() <= targetCourtCount) {
                occupiedCourts.add(game.getCourt());
                continue;
            }
            gamesToRelocate.add(game);
        }

        for (Game game : gamesToRelocate) {
            Integer emptyCourt = findEmptyCourt(targetCourtCount, occupiedCourts);
            if (emptyCourt == null) {
                throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
            }
            game.assignCourt(emptyCourt);
            occupiedCourts.add(emptyCourt);
        }
    }

    private Integer findEmptyCourt(int courtCount, Set<Integer> occupiedCourts) {
        for (int court = 1; court <= courtCount; court++) {
            if (!occupiedCourts.contains(court)) {
                return court;
            }
        }
        return null;
    }

    private List<CompetitionEntry> resolveTeamEntries(
            Competition competition,
            List<GameEntryUpdateRequest.PlayerRequest> playerRequests
    ) {
        if (playerRequests == null || playerRequests.size() != 2) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }

        List<CompetitionEntry> entries = new ArrayList<>();
        for (GameEntryUpdateRequest.PlayerRequest playerRequest : playerRequests) {
            if (playerRequest.getCompetitionEntryId() == null) {
                throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
            }
            CompetitionEntry entry = competitionEntryRepository
                    .findByIdAndCompetitionId(playerRequest.getCompetitionEntryId(), competition.getId())
                    .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
            entries.add(entry);
        }
        return entries;
    }

    private void validateDistinctPlayers(List<CompetitionEntry> teamA, List<CompetitionEntry> teamB) {
        Set<Long> entryIds = new HashSet<>();
        for (CompetitionEntry entry : teamA) {
            entryIds.add(entry.getId());
        }
        for (CompetitionEntry entry : teamB) {
            entryIds.add(entry.getId());
        }
        if (entryIds.size() != 4) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateClubSessionGameEntries(
            Competition competition,
            List<CompetitionEntry> teamA,
            List<CompetitionEntry> teamB
    ) {
        for (CompetitionEntry entry : teamA) {
            validateActiveEntry(entry);
        }
        for (CompetitionEntry entry : teamB) {
            validateActiveEntry(entry);
        }
    }

    private void validateActiveEntry(CompetitionEntry entry) {
        if (entry.getStatus() != CompetitionEntry.EntryStatus.ACTIVE) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private Game.GameStatus resolveGameStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
        try {
            return Game.GameStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private List<GameEntry> createGameEntries(
            Game game,
            List<CompetitionEntry> teamA,
            List<CompetitionEntry> teamB
    ) {
        List<GameEntry> gameEntries = new ArrayList<>();
        for (int i = 0; i < teamA.size(); i++) {
            gameEntries.add(new GameEntry(game, teamA.get(i), GameEntry.Team.A, i + 1));
        }
        for (int i = 0; i < teamB.size(); i++) {
            gameEntries.add(new GameEntry(game, teamB.get(i), GameEntry.Team.B, i + 1));
        }
        return gameEntries;
    }

    private Game.MatchType resolveMatchType(List<CompetitionEntry> teamA, List<CompetitionEntry> teamB) {
        long teamAMaleCount = countMalePlayers(teamA);
        long teamBMaleCount = countMalePlayers(teamB);
        long maleCount = teamAMaleCount + teamBMaleCount;
        long femaleCount = teamA.size() + teamB.size() - maleCount;

        if (teamAMaleCount == 1 && teamBMaleCount == 1) {
            return Game.MatchType.MIXED;
        }
        if (teamAMaleCount == 2 && teamBMaleCount == 2) {
            return Game.MatchType.MALE;
        }
        if (teamAMaleCount == 0 && teamBMaleCount == 0) {
            return Game.MatchType.FEMALE;
        }
        if ((teamAMaleCount == 2 && teamBMaleCount == 0) || (teamAMaleCount == 0 && teamBMaleCount == 2)) {
            return Game.MatchType.M2F2_SPLIT;
        }
        if (maleCount == 3 && femaleCount == 1) {
            return Game.MatchType.RANDOM_M3F1;
        }
        if (maleCount == 1 && femaleCount == 3) {
            return Game.MatchType.RANDOM_M1F3;
        }

        throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
    }

    private long countMalePlayers(List<CompetitionEntry> entries) {
        return entries.stream()
                .filter(entry -> entry.getGender() == CompetitionEntry.Gender.MALE)
                .count();
    }

    private CompetitionStat recalculateCompetitionStat(Competition competition) {
        List<CompetitionEntry> competitionEntries =
                competitionEntryRepository.findByCompetitionId(competition.getId());
        List<GameEntry> scheduleEntries =
                gameEntryRepository.findScheduleEntriesByCompetitionId(competition.getId());

        Map<Long, Integer> gameCountByEntryId = new HashMap<>();
        for (CompetitionEntry entry : competitionEntries) {
            gameCountByEntryId.put(entry.getId(), 0);
        }

        Map<Long, Game.MatchType> matchTypeByGameId = new HashMap<>();
        for (GameEntry gameEntry : scheduleEntries) {
            matchTypeByGameId.putIfAbsent(
                    gameEntry.getGame().getId(),
                    gameEntry.getGame().getMatchType()
            );
            gameCountByEntryId.merge(gameEntry.getCompetitionEntry().getId(), 1, Integer::sum);
        }

        int totalGames = matchTypeByGameId.size();
        int mixedCount = 0;
        int maleCount = 0;
        int femaleCount = 0;
        int m2f2SplitCount = 0;
        int randomM3F1Count = 0;
        int randomM1F3Count = 0;

        for (Game.MatchType matchType : matchTypeByGameId.values()) {
            switch (matchType) {
                case MIXED -> mixedCount++;
                case MALE -> maleCount++;
                case FEMALE -> femaleCount++;
                case M2F2_SPLIT -> m2f2SplitCount++;
                case RANDOM_M3F1 -> randomM3F1Count++;
                case RANDOM_M1F3 -> randomM1F3Count++;
            }
        }

        int maxGames = gameCountByEntryId.values().stream().max(Integer::compareTo).orElse(0);
        int minGames = gameCountByEntryId.values().stream().min(Integer::compareTo).orElse(0);

        CompetitionStat stat = competitionStatRepository.findByCompetitionId(competition.getId())
                .orElseGet(() -> new CompetitionStat(competition));
        stat.replaceStatistics(
                totalGames,
                mixedCount,
                maleCount,
                femaleCount,
                m2f2SplitCount,
                randomM3F1Count,
                randomM1F3Count,
                maxGames,
                minGames
        );

        return competitionStatRepository.save(stat);
    }
}
