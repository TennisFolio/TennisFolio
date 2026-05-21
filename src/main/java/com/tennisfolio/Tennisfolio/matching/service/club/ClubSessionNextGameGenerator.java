package com.tennisfolio.Tennisfolio.matching.service.club;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class ClubSessionNextGameGenerator {

    private static final int MAX_CONSECUTIVE_GAMES_BEFORE_REST = 2;
    private static final int RECENT_WINDOW_SIZE = 4;
    private static final int IMMEDIATE_REPEAT_PENALTY = 220;
    private static final int SECOND_RECENT_PENALTY = 100;
    private static final int THIRD_RECENT_PENALTY = 50;
    private static final int RECENT_WINDOW_PENALTY = 70;
    private static final int REST_BONUS = 15;

    private final ScoreCalculator scoreCalculator;
    private final CandidateGenerator generator;
    private Random random;

    public ClubSessionNextGameGenerator(ScoreCalculator scoreCalculator, CandidateGenerator generator) {
        this.scoreCalculator = scoreCalculator;
        this.generator = generator;
    }

    public GameMatch generateNextGame(
            List<CompetitionEntry> candidateEntries,
            List<GameEntry> history,
            int court,
            int round,
            long seed
    ) {
        this.random = new Random(seed + (court * 31L) + round);
        List<GamePlayer> players = createPlayers(candidateEntries);
        Map<String, GamePlayer> playersById = new HashMap<>();
        for (GamePlayer player : players) {
            playersById.put(player.id, player);
        }

        int male = countPlayers(players, GamePlayer.Gender.MALE);
        int female = countPlayers(players, GamePlayer.Gender.FEMALE);
        boolean allowRandom = !canScheduleWithoutRandom(male, female, 1)
                && canScheduleRandomType(male, female);

        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
        for (MatchType t : MatchType.values()) {
            typeCount.put(t, 0);
        }
        Map<Set<String>, Integer> groupCount = new HashMap<>();
        PlayerHistory playerHistory = applyHistory(history, playersById, typeCount, groupCount);

        BestCandidate best = new BestCandidate();
        BestCandidate bestWithoutThreeStraight = new BestCandidate();
        generator.forEachCandidate(players, allowRandom, candidate -> {
            int score = scoreCalculator.scoreWithoutTotalGamePenalty(
                    candidate,
                    typeCount,
                    Set.of(),
                    round,
                    round,
                    groupCount,
                    male,
                    female
            );
            score += scoreClubSessionFairness(candidate, playerHistory);
            int tieBreaker = random.nextInt();

            best.consider(candidate, score, tieBreaker);
            if (!createsThreeStraightGame(candidate, playerHistory)) {
                bestWithoutThreeStraight.consider(candidate, score, tieBreaker);
            }
        });

        BestCandidate selected = bestWithoutThreeStraight.candidate != null ? bestWithoutThreeStraight : best;
        if (selected.candidate == null) {
            throw new NoSuchElementException("No valid match candidate");
        }

        return new GameMatch(round, court, selected.candidate.type, selected.candidate.teamA, selected.candidate.teamB);
    }

    private boolean createsThreeStraightGame(MatchCandidate candidate, PlayerHistory playerHistory) {
        for (GamePlayer player : candidate.allPlayers()) {
            if (playerHistory.currentConsecutiveGames(player.id) >= MAX_CONSECUTIVE_GAMES_BEFORE_REST) {
                return true;
            }
        }
        return false;
    }

    private int scoreClubSessionFairness(MatchCandidate candidate, PlayerHistory playerHistory) {
        int score = 0;
        int currentSequence = playerHistory.currentSequence();
        for (GamePlayer player : candidate.allPlayers()) {
            int lastPlayedSequence = playerHistory.lastPlayedSequence(player.id);
            int gamesAgo = lastPlayedSequence == 0 ? Integer.MAX_VALUE : currentSequence - lastPlayedSequence;

            score -= playerHistory.recentWindowCount(player.id) * RECENT_WINDOW_PENALTY;

            if (gamesAgo == 1) {
                score -= IMMEDIATE_REPEAT_PENALTY;
            } else if (gamesAgo == 2) {
                score -= SECOND_RECENT_PENALTY;
            } else if (gamesAgo == 3) {
                score -= THIRD_RECENT_PENALTY;
            } else if (gamesAgo != Integer.MAX_VALUE) {
                score += Math.min(gamesAgo, 4) * REST_BONUS;
            }
        }
        return score;
    }

    private List<GamePlayer> createPlayers(List<CompetitionEntry> entries) {
        List<GamePlayer> list = new ArrayList<>();
        for (CompetitionEntry entry : entries) {
            GamePlayer.Gender gender = entry.getGender() == CompetitionEntry.Gender.MALE
                    ? GamePlayer.Gender.MALE
                    : GamePlayer.Gender.FEMALE;
            list.add(new GamePlayer(String.valueOf(entry.getId()), gender));
        }
        return list;
    }

    private int countPlayers(List<GamePlayer> players, GamePlayer.Gender gender) {
        int count = 0;
        for (GamePlayer player : players) {
            if (player.gender == gender) {
                count++;
            }
        }
        return count;
    }

    private PlayerHistory applyHistory(
            List<GameEntry> history,
            Map<String, GamePlayer> playersById,
            Map<MatchType, Integer> typeCount,
            Map<Set<String>, Integer> groupCount
    ) {
        List<List<GameEntry>> gamesInOrder = groupHistoryGames(history);
        PlayerHistory playerHistory = new PlayerHistory();
        int sequence = 0;
        int recentThreshold = Math.max(1, gamesInOrder.size() + 1 - RECENT_WINDOW_SIZE);

        for (List<GameEntry> entriesInGame : gamesInOrder) {
            if (entriesInGame.size() != 4) {
                continue;
            }
            sequence++;

            List<GamePlayer> teamA = new ArrayList<>();
            List<GamePlayer> teamB = new ArrayList<>();
            List<String> entryIdsInGame = new ArrayList<>();
            for (GameEntry gameEntry : entriesInGame) {
                String entryId = String.valueOf(gameEntry.getCompetitionEntry().getId());
                entryIdsInGame.add(entryId);
                GamePlayer player = playersById.get(entryId);
                if (player == null) {
                    continue;
                }
                if (gameEntry.getTeam() == GameEntry.Team.A) {
                    teamA.add(player);
                } else {
                    teamB.add(player);
                }
            }

            playerHistory.record(sequence, entryIdsInGame, recentThreshold);

            if (teamA.size() != 2 || teamB.size() != 2) {
                continue;
            }

            MatchType matchType = toDomainMatchType(entriesInGame.get(0).getGame().getMatchType());
            apply(new MatchCandidate(matchType, teamA, teamB), groupCount);
            typeCount.put(matchType, typeCount.get(matchType) + 1);
        }

        playerHistory.setCurrentSequence(sequence + 1);
        return playerHistory;
    }

    private List<List<GameEntry>> groupHistoryGames(List<GameEntry> history) {
        Map<Long, List<GameEntry>> entriesByGameId = new LinkedHashMap<>();
        for (GameEntry gameEntry : history) {
            entriesByGameId.computeIfAbsent(
                    gameEntry.getGame().getId(),
                    ignored -> new ArrayList<>()
            ).add(gameEntry);
        }

        return entriesByGameId.values().stream()
                .sorted(Comparator.comparing(this::historyGameTime)
                        .thenComparing(entries -> entries.get(0).getGame().getId()))
                .toList();
    }

    private LocalDateTime historyGameTime(List<GameEntry> entries) {
        Game game = entries.get(0).getGame();
        if (game.getUpdateDt() != null) {
            return game.getUpdateDt();
        }
        if (game.getCreateDt() != null) {
            return game.getCreateDt();
        }
        return LocalDateTime.MIN;
    }

    private MatchType toDomainMatchType(Game.MatchType matchType) {
        return switch (matchType) {
            case MIXED -> MatchType.MIXED;
            case MALE -> MatchType.MALE;
            case FEMALE -> MatchType.FEMALE;
            case RANDOM_M3F1 -> MatchType.RANDOM_M3F1;
            case RANDOM_M1F3 -> MatchType.RANDOM_M1F3;
            case M2F2_SPLIT -> MatchType.MIXED;
        };
    }

    private void apply(MatchCandidate c, Map<Set<String>, Integer> groupCount) {
        List<GamePlayer> teamA = c.teamA;
        List<GamePlayer> teamB = c.teamB;
        List<GamePlayer> allPlayers = c.allPlayers();

        for (GamePlayer p : allPlayers) {
            p.totalGames++;
        }

        for (GamePlayer p1 : teamA) {
            for (GamePlayer p2 : teamA) {
                if (p1 != p2) {
                    p1.partnerCount.merge(p2.id, 1, Integer::sum);
                }
            }
        }

        for (GamePlayer p1 : teamB) {
            for (GamePlayer p2 : teamB) {
                if (p1 != p2) {
                    p1.partnerCount.merge(p2.id, 1, Integer::sum);
                }
            }
        }

        for (GamePlayer p1 : teamA) {
            for (GamePlayer p2 : teamB) {
                p1.opponentCount.merge(p2.id, 1, Integer::sum);
                p2.opponentCount.merge(p1.id, 1, Integer::sum);
            }
        }

        for (GamePlayer p : allPlayers) {
            p.typeExperience.merge(c.type, 1, Integer::sum);
        }

        Set<String> group = new HashSet<>(4);
        group.add(allPlayers.get(0).id);
        group.add(allPlayers.get(1).id);
        group.add(allPlayers.get(2).id);
        group.add(allPlayers.get(3).id);

        groupCount.merge(group, 1, Integer::sum);
    }

    private boolean canScheduleWithoutRandom(int male, int female, int court) {
        for (int mixed = 0; mixed <= court; mixed++) {
            int remainCourts = court - mixed;

            for (int maleMatch = 0; maleMatch <= remainCourts; maleMatch++) {
                int femaleMatch = remainCourts - maleMatch;

                int maleNeeded = mixed * 2 + maleMatch * 4;
                int femaleNeeded = mixed * 2 + femaleMatch * 4;

                if (maleNeeded <= male && femaleNeeded <= female) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canScheduleRandomType(int male, int female) {
        return (male >= 3 && female >= 1) || (male >= 1 && female >= 3);
    }

    private static class PlayerHistory {
        private final Map<String, Integer> lastPlayedSequenceByEntryId = new HashMap<>();
        private final Map<String, Integer> recentWindowCountByEntryId = new HashMap<>();
        private final Map<String, Integer> consecutiveGamesByEntryId = new HashMap<>();
        private int currentSequence = 1;

        private void record(int sequence, List<String> entryIds, int recentThreshold) {
            for (String entryId : entryIds) {
                int previousSequence = lastPlayedSequenceByEntryId.getOrDefault(entryId, 0);
                int previousConsecutiveGames = consecutiveGamesByEntryId.getOrDefault(entryId, 0);
                int consecutiveGames = previousSequence == sequence - 1 ? previousConsecutiveGames + 1 : 1;

                lastPlayedSequenceByEntryId.put(entryId, sequence);
                consecutiveGamesByEntryId.put(entryId, consecutiveGames);
            }
            if (sequence >= recentThreshold) {
                for (String entryId : entryIds) {
                    recentWindowCountByEntryId.merge(entryId, 1, Integer::sum);
                }
            }
        }

        private void setCurrentSequence(int currentSequence) {
            this.currentSequence = currentSequence;
        }

        private int currentSequence() {
            return currentSequence;
        }

        private int lastPlayedSequence(String entryId) {
            return lastPlayedSequenceByEntryId.getOrDefault(entryId, 0);
        }

        private int recentWindowCount(String entryId) {
            return recentWindowCountByEntryId.getOrDefault(entryId, 0);
        }

        private int currentConsecutiveGames(String entryId) {
            if (lastPlayedSequence(entryId) != currentSequence - 1) {
                return 0;
            }
            return consecutiveGamesByEntryId.getOrDefault(entryId, 0);
        }
    }

    private static class BestCandidate {
        private MatchCandidate candidate;
        private int score = Integer.MIN_VALUE;
        private int tieBreaker = Integer.MIN_VALUE;

        private void consider(MatchCandidate candidate, int score, int tieBreaker) {
            if (this.candidate == null || score > this.score || (score == this.score && tieBreaker > this.tieBreaker)) {
                this.candidate = candidate;
                this.score = score;
                this.tieBreaker = tieBreaker;
            }
        }
    }
}
