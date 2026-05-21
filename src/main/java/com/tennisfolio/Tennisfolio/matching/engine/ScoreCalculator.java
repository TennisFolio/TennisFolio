package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ScoreCalculator {

    public int score(
            MatchCandidate c,
            Map<MatchType, Integer> typeCount,
            Set<MatchType> roundTypes,
            int round,
            int totalRounds,
            Map<Set<String>, Integer> groupCount,
            int maleCount,
            int femaleCount
    ) {
        return score(c, typeCount, roundTypes, round, totalRounds, groupCount, maleCount, femaleCount, true);
    }

    public int scoreWithoutTotalGamePenalty(
            MatchCandidate c,
            Map<MatchType, Integer> typeCount,
            Set<MatchType> roundTypes,
            int round,
            int totalRounds,
            Map<Set<String>, Integer> groupCount,
            int maleCount,
            int femaleCount
    ) {
        return score(c, typeCount, roundTypes, round, totalRounds, groupCount, maleCount, femaleCount, false);
    }

    private int score(
            MatchCandidate c,
            Map<MatchType, Integer> typeCount,
            Set<MatchType> roundTypes,
            int round,
            int totalRounds,
            Map<Set<String>, Integer> groupCount,
            int maleCount,
            int femaleCount,
            boolean includeTotalGamePenalty
    ) {
        int score = 0;
        List<GamePlayer> players = c.allPlayers();
        MatchType type = c.type;

        for (GamePlayer p : players) {
            if (includeTotalGamePenalty) {
                score -= p.totalGames * 300;
            }
            score -= p.consecutiveRounds * 30;
        }

        score -= typeCount.get(type) * 100;

        boolean randomType = isRandomType(type);
        boolean alreadyHasRandomInRound = roundTypes.contains(MatchType.RANDOM_M3F1)
                || roundTypes.contains(MatchType.RANDOM_M1F3);
        if (randomType && alreadyHasRandomInRound) {
            score -= 2000;
        }

        if (randomType) {
            score -= (totalRounds - round) * 200;
        }

        if (canDiversifyGroup(c, maleCount, femaleCount)) {
            score -= groupCount.getOrDefault(groupKey(players), 0) * 500;
        }

        score -= partnerRepeatPenalty(c) * 200;
        score -= opponentRepeatPenalty(c) * 50;

        for (GamePlayer p : players) {
            int exp = p.typeExperience.getOrDefault(type, 0);
            score -= exp * 15;
        }

        if (randomType) {
            for (GamePlayer p : players) {
                int randomExp =
                        p.typeExperience.getOrDefault(MatchType.RANDOM_M3F1, 0)
                                + p.typeExperience.getOrDefault(MatchType.RANDOM_M1F3, 0);

                int normalExp =
                        p.typeExperience.getOrDefault(MatchType.MIXED, 0)
                                + p.typeExperience.getOrDefault(MatchType.MALE, 0)
                                + p.typeExperience.getOrDefault(MatchType.FEMALE, 0);

                if (randomExp > normalExp) {
                    score -= 100;
                }
            }
        }

        for (GamePlayer p : players) {
            score += experiencedTypeCount(p) * 2;
        }

        return score;
    }

    private int partnerRepeatPenalty(MatchCandidate c) {
        int penalty = 0;

        penalty += partnerCount(c.teamA.get(0), c.teamA.get(1));
        penalty += partnerCount(c.teamB.get(0), c.teamB.get(1));

        return penalty;
    }

    private int opponentRepeatPenalty(MatchCandidate c) {
        int penalty = 0;

        for (GamePlayer a : c.teamA) {
            for (GamePlayer b : c.teamB) {
                penalty += a.opponentCount.getOrDefault(b.id, 0);
            }
        }

        return penalty;
    }

    private int partnerCount(GamePlayer a, GamePlayer b) {
        return a.partnerCount.getOrDefault(b.id, 0);
    }

    private boolean isRandomType(MatchType t) {
        return t == MatchType.RANDOM_M3F1 || t == MatchType.RANDOM_M1F3;
    }

    private int experiencedTypeCount(GamePlayer p) {
        int count = 0;
        for (MatchType type : MatchType.values()) {
            if (p.typeExperience.getOrDefault(type, 0) > 0) {
                count++;
            }
        }
        return count;
    }

    private Set<String> groupKey(List<GamePlayer> players) {
        Set<String> group = new HashSet<>(4);
        group.add(players.get(0).id);
        group.add(players.get(1).id);
        group.add(players.get(2).id);
        group.add(players.get(3).id);
        return group;
    }

    private boolean canDiversifyGroup(MatchCandidate c, int male, int female) {
        return switch (c.type) {
            case MALE -> male > 4;
            case FEMALE -> female > 4;
            case MIXED -> male > 2 && female > 2;
            case RANDOM_M3F1, RANDOM_M1F3 -> true;
        };
    }
}
