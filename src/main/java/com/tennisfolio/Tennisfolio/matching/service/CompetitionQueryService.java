package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionDetailResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionRankingResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionResultResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class CompetitionQueryService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionEntryRepository competitionEntryRepository;
    private final CompetitionStatRepository competitionStatRepository;
    private final GameEntryRepository gameEntryRepository;

    public CompetitionQueryService(
            CompetitionRepository competitionRepository,
            CompetitionEntryRepository competitionEntryRepository,
            CompetitionStatRepository competitionStatRepository,
            GameEntryRepository gameEntryRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.competitionEntryRepository = competitionEntryRepository;
        this.competitionStatRepository = competitionStatRepository;
        this.gameEntryRepository = gameEntryRepository;
    }

    @Transactional(readOnly = true)
    public CompetitionDetailResponse getCompetition(String publicId) {
        return getCompetition(publicId, null);
    }

    @Transactional(readOnly = true)
    public CompetitionDetailResponse getCompetition(String publicId, Long currentUserId) {
        Competition competition = findActiveCompetition(publicId);
        CompetitionStat stat = competitionStatRepository.findByCompetitionId(competition.getId()).orElse(null);
        List<GameEntry> gameEntries = gameEntryRepository.findScheduleEntriesByCompetitionId(competition.getId());

        return CompetitionDetailResponse.from(competition, stat, gameEntries, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<CompetitionSummaryResponse> getOwnedCompetitions(Long ownerUserId) {
        return competitionRepository.findByOwnerUserIdOrderByCreateDtDesc(ownerUserId)
                .stream()
                .map(CompetitionSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompetitionResultResponse getCompetitionResult(String publicId) {
        Competition competition = findActiveCompetition(publicId);
        List<CompetitionEntry> entries = competitionEntryRepository.findByCompetitionId(competition.getId());
        List<GameEntry> gameEntries = gameEntryRepository.findScheduleEntriesByCompetitionId(competition.getId());
        Map<Long, List<GameEntry>> gameEntriesByGameId = groupGameEntriesByGameId(gameEntries);

        List<GameResultAggregate> aggregates = aggregateResults(entries, gameEntriesByGameId);

        return new CompetitionResultResponse(
                competition.getPublicId(),
                competition.getName(),
                gameEntriesByGameId.size(),
                countCompletedGames(gameEntriesByGameId),
                new CompetitionResultResponse.Rankings(
                        toRankings(aggregates, ignored -> true),
                        toRankings(aggregates, aggregate -> aggregate.entry.getGender() == CompetitionEntry.Gender.MALE),
                        toRankings(aggregates, aggregate -> aggregate.entry.getGender() == CompetitionEntry.Gender.FEMALE)
                )
        );
    }

    private Competition findActiveCompetition(String publicId) {
        return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private Map<Long, List<GameEntry>> groupGameEntriesByGameId(List<GameEntry> gameEntries) {
        Map<Long, List<GameEntry>> gameEntriesByGameId = new LinkedHashMap<>();
        for (GameEntry gameEntry : gameEntries) {
            gameEntriesByGameId.computeIfAbsent(
                    gameEntry.getGame().getId(),
                    ignored -> new ArrayList<>()
            ).add(gameEntry);
        }
        return gameEntriesByGameId;
    }

    private List<GameResultAggregate> aggregateResults(
            List<CompetitionEntry> entries,
            Map<Long, List<GameEntry>> gameEntriesByGameId
    ) {
        Map<Long, GameResultAggregate> aggregateByEntryId = new LinkedHashMap<>();
        for (CompetitionEntry entry : entries) {
            aggregateByEntryId.put(entry.getId(), new GameResultAggregate(entry));
        }

        for (List<GameEntry> entriesInGame : gameEntriesByGameId.values()) {
            if (entriesInGame.isEmpty()) {
                continue;
            }

            Game game = entriesInGame.get(0).getGame();
            if (!isCompletedGame(game)) {
                continue;
            }

            Boolean isTeamAWinner = resolveTeamAWinner(game);
            for (GameEntry gameEntry : entriesInGame) {
                GameResultAggregate aggregate = aggregateByEntryId.get(
                        gameEntry.getCompetitionEntry().getId()
                );
                if (aggregate == null) {
                    continue;
                }

                boolean isTeamA = gameEntry.getTeam() == GameEntry.Team.A;
                Boolean isWinner = isTeamAWinner == null ? null : isTeamA == isTeamAWinner;
                aggregate.recordGame(
                        isWinner,
                        isTeamA ? game.getTeamAScore() : game.getTeamBScore(),
                        isTeamA ? game.getTeamBScore() : game.getTeamAScore(),
                        isTeamA ? safeScore(game.getTeamATiebreaKScore()) : safeScore(game.getTeamBTiebreaKScore()),
                        isTeamA ? safeScore(game.getTeamBTiebreaKScore()) : safeScore(game.getTeamATiebreaKScore())
                );
            }
        }

        return new ArrayList<>(aggregateByEntryId.values());
    }

    private boolean isCompletedGame(Game game) {
        int teamAScore = safeScore(game.getTeamAScore());
        int teamBScore = safeScore(game.getTeamBScore());

        return teamAScore != 0 || teamBScore != 0;
    }

    private Boolean resolveTeamAWinner(Game game) {
        int teamAScore = safeScore(game.getTeamAScore());
        int teamBScore = safeScore(game.getTeamBScore());

        if (teamAScore == teamBScore) {
            return null;
        }

        return teamAScore > teamBScore;
    }

    private int countCompletedGames(Map<Long, List<GameEntry>> gameEntriesByGameId) {
        int completedGames = 0;
        for (List<GameEntry> entriesInGame : gameEntriesByGameId.values()) {
            if (!entriesInGame.isEmpty() && isCompletedGame(entriesInGame.get(0).getGame())) {
                completedGames++;
            }
        }
        return completedGames;
    }

    private int safeScore(Integer score) {
        return score == null ? 0 : score;
    }

    private List<CompetitionRankingResponse> toRankings(
            List<GameResultAggregate> aggregates,
            Predicate<GameResultAggregate> filter
    ) {
        List<GameResultAggregate> sortedAggregates = aggregates.stream()
                .filter(filter)
                .sorted(resultComparator())
                .toList();

        List<CompetitionRankingResponse> rankings = new ArrayList<>();
        for (int index = 0; index < sortedAggregates.size(); index++) {
            GameResultAggregate aggregate = sortedAggregates.get(index);
            rankings.add(CompetitionRankingResponse.from(
                    index + 1,
                    aggregate.entry,
                    aggregate.gamesPlayed,
                    aggregate.wins,
                    aggregate.losses,
                    aggregate.draws,
                    aggregate.pointsFor,
                    aggregate.pointsAgainst,
                    aggregate.tiebreakPointsFor,
                    aggregate.tiebreakPointsAgainst
            ));
        }
        return rankings;
    }

    private Comparator<GameResultAggregate> resultComparator() {
        return Comparator.comparingDouble(GameResultAggregate::rankingPointRate).reversed()
                .thenComparing(Comparator.comparingInt(GameResultAggregate::rankingPoints).reversed())
                .thenComparingInt(GameResultAggregate::losses)
                .thenComparing(Comparator.comparingInt(GameResultAggregate::pointDiff).reversed())
                .thenComparing(Comparator.comparingInt(GameResultAggregate::gamesPlayed).reversed())
                .thenComparing(Comparator.comparingInt(GameResultAggregate::tiebreakPointDiff).reversed())
                .thenComparing(aggregate -> aggregate.entry.getPlayerName())
                .thenComparing(aggregate -> aggregate.entry.getId());
    }

    private static class GameResultAggregate {
        private final CompetitionEntry entry;
        private int gamesPlayed;
        private int wins;
        private int losses;
        private int draws;
        private int pointsFor;
        private int pointsAgainst;
        private int tiebreakPointsFor;
        private int tiebreakPointsAgainst;

        private GameResultAggregate(CompetitionEntry entry) {
            this.entry = entry;
        }

        private void recordGame(
                Boolean isWinner,
                int scoreFor,
                int scoreAgainst,
                int tiebreakScoreFor,
                int tiebreakScoreAgainst
        ) {
            gamesPlayed++;
            if (isWinner == null) {
                draws++;
            } else if (isWinner) {
                wins++;
            } else {
                losses++;
            }
            pointsFor += scoreFor;
            pointsAgainst += scoreAgainst;
            tiebreakPointsFor += tiebreakScoreFor;
            tiebreakPointsAgainst += tiebreakScoreAgainst;
        }

        private double winRate() {
            return gamesPlayed == 0 ? 0.0 : (double) wins / gamesPlayed;
        }

        private int rankingPoints() {
            return wins * 2 + draws;
        }

        private double rankingPointRate() {
            return gamesPlayed == 0 ? 0.0 : (double) rankingPoints() / gamesPlayed;
        }

        private int pointDiff() {
            return pointsFor - pointsAgainst;
        }

        private int gamesPlayed() {
            return gamesPlayed;
        }

        private int losses() {
            return losses;
        }

        private int tiebreakPointDiff() {
            return tiebreakPointsFor - tiebreakPointsAgainst;
        }
    }
}
