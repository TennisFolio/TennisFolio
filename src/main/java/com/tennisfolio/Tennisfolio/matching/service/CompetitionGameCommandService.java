package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionStatResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameBalanceResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameEntryUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CompetitionGameCommandService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;
    private final CompetitionStatRepository competitionStatRepository;
    private final GameRepository gameRepository;
    private final GameEntryRepository gameEntryRepository;

    public CompetitionGameCommandService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository,
            CompetitionStatRepository competitionStatRepository,
            GameRepository gameRepository,
            GameEntryRepository gameEntryRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
        this.competitionStatRepository = competitionStatRepository;
        this.gameRepository = gameRepository;
        this.gameEntryRepository = gameEntryRepository;
    }

    @Transactional
    public GameEntryUpdateResponse updateGameEntries(
            String publicId,
            Long gameId,
            String editToken,
            GameEntryUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        validateEditToken(competition, editToken);
        validateReadyCompetition(competition);

        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        List<CompetitionEntry> teamA = resolveTeamEntries(competition, request.getTeamA());
        List<CompetitionEntry> teamB = resolveTeamEntries(competition, request.getTeamB());
        validateDistinctPlayers(teamA, teamB);

        game.updateMatchType(resolveMatchType(teamA, teamB));

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
            GameScoreUpdateRequest request
    ) {
        Competition competition = competitionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        Game game = gameRepository.findByIdAndCompetitionId(gameId, competition.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));

        Integer teamAScore = normalizeScore(request.getTeamAScore());
        Integer teamBScore = normalizeScore(request.getTeamBScore());
        Integer teamATiebreakScore = normalizeScore(request.getTeamATiebreakScore());
        Integer teamBTiebreakScore = normalizeScore(request.getTeamBTiebreakScore());

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

    private void validateReadyCompetition(Competition competition) {
        if (competition.getStatus() != Competition.CompetitionStatus.READY) {
            throw new InvalidRequestException(ExceptionCode.INVALID_REQUEST);
        }
    }

    private void validateEditToken(Competition competition, String editToken) {
        if (editToken == null || !competition.getEditToken().equals(editToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid edit token");
        }
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
