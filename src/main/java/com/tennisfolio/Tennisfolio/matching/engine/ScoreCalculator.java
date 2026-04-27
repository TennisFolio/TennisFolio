package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        int score = 0;

        for (GamePlayer p : c.allPlayers()) {
            score -= p.totalGames * 300;
            score -= p.consecutiveRounds * 30;
        }

        // 타입 쏠림 방지
        score -= typeCount.get(c.type) * 100;

        boolean randomType = isRandomType(c.type);

        // 같은 라운드에 RANDOM_M1F3 + RANDOM_M3F1 같이 나오는 것 방지
        boolean alreadyHasRandomInRound = roundTypes.stream().anyMatch(this::isRandomType);
        if (randomType && alreadyHasRandomInRound) {
            score -= 2000;
        }

        // RANDOM은 후반으로 미룸
        if (randomType) {
            score -= (totalRounds - round) * 200;
        }

        // 4명 그룹 중복: 그룹 다양화가 가능한 경우에만 적용
        if (canDiversifyGroup(c, maleCount, femaleCount)) {
            Set<String> group = c.allPlayers().stream()
                    .map(p -> p.id)
                    .collect(Collectors.toSet());

            score -= groupCount.getOrDefault(group, 0) * 500;
        }

        // 파트너 중복: 4명만 있어도 팀 조합은 바꿀 수 있으므로 항상 적용
        score -= partnerRepeatPenalty(c) * 200;

        // 상대 중복: 파트너보다는 약하게 적용
        score -= opponentRepeatPenalty(c) * 50;

        List<GamePlayer> players = c.allPlayers();
        MatchType type = c.type;

        // 🔥 1. 타입 경험 편차 패널티
        for (GamePlayer p : players) {
            int exp = p.typeExperience.getOrDefault(type, 0);

            // 많이 했을수록 강하게 패널티
            score -= exp * 15;
        }

        // 🔥 2. RANDOM 희생 방지 (핵심)
        if (type == MatchType.RANDOM_M3F1 || type == MatchType.RANDOM_M1F3) {

            for (GamePlayer p : players) {

                int randomExp =
                        p.typeExperience.getOrDefault(MatchType.RANDOM_M3F1, 0)
                                + p.typeExperience.getOrDefault(MatchType.RANDOM_M1F3, 0);

                int normalExp =
                        p.typeExperience.getOrDefault(MatchType.MIXED, 0)
                                + p.typeExperience.getOrDefault(MatchType.MALE, 0)
                                + p.typeExperience.getOrDefault(MatchType.FEMALE, 0);

                // 👉 RANDOM만 하는 사람 방지
                if (randomExp > normalExp) {
                    score -= 100; // 강한 패널티
                }
            }
        }

        // 🔥 3. 타입 다양성 보너스
        for (GamePlayer p : players) {

            long experiencedTypes = p.typeExperience.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .count();

            // 다양한 타입 경험한 사람 우대
            score += experiencedTypes * 2;
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

    private boolean canDiversifyGroup(MatchCandidate c, int male, int female) {
        return switch (c.type) {
            case MALE -> male > 4;
            case FEMALE -> female > 4;
            case MIXED -> male > 2 && female > 2;
            case RANDOM_M3F1, RANDOM_M1F3 -> true;
        };
    }
}
