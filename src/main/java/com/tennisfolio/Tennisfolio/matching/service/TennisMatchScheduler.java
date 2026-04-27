package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.*;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ConstraintChecker;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TennisMatchScheduler {

    private final ConstraintChecker constraintChecker;
    private final ScoreCalculator scoreCalculator;
    private final CandidateGenerator generator;
    private final Random random;


    public TennisMatchScheduler(ConstraintChecker constraintChecker, ScoreCalculator scoreCalculator, CandidateGenerator generator, long seed) {
        this.constraintChecker = constraintChecker;
        this.scoreCalculator = scoreCalculator;
        this.generator = generator;
        this.random = new Random(seed);
    }

    public ScheduleResult generate(int male, int female, int court, int rounds) {

        List<GamePlayer> players = createPlayers(male, female);

        int totalMatches = court * rounds;
        int totalSlots = totalMatches * 4;

        int maxGames = (int) Math.ceil((double) totalSlots / players.size());

        ScheduleResult result = new ScheduleResult();

        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
        for (MatchType t : MatchType.values()) {
            typeCount.put(t, 0);
        }

        // 🔥 중요: generate마다 초기화
        Map<Set<String>, Integer> groupCount = new HashMap<>();

        boolean allowRandom = !canScheduleWithoutRandom(male, female, court);

        for (int r = 1; r <= rounds; r++) {

            Set<GamePlayer> used = new HashSet<>();
            Set<GamePlayer> playedThisRound = new HashSet<>();
            Set<MatchType> roundTypes = new HashSet<>();

            final int currentRound = r;

            for (int c = 1; c <= court; c++) {

                List<MatchCandidate> candidates = generator.generate(players);

                MatchCandidate best = candidates.stream()

                        // 1️⃣ 기본 constraint
                        .filter(x -> constraintChecker.isValid(x, players, used, court, maxGames))

                        // 2️⃣ RANDOM 제어
                        .filter(x -> {
                            if (!allowRandom && isRandomType(x.type)) {
                                return false;
                            }
                            return true;
                        })

                        // 3️⃣ Score 기반 선택
                        .max(Comparator
                                .comparingInt((MatchCandidate x) ->
                                        scoreCalculator.score(
                                                x,
                                                typeCount,
                                                roundTypes,
                                                currentRound,
                                                rounds,
                                                groupCount,
                                                male,
                                                female
                                        ))
                                .thenComparingInt(x -> random.nextInt()))
                        .orElseThrow();

                apply(best, groupCount);

                // 타입 기록
                typeCount.put(best.type, typeCount.get(best.type) + 1);
                roundTypes.add(best.type);

                result.matches.add(new GameMatch(r, c, best.type, best.teamA, best.teamB));

                used.addAll(best.allPlayers());
                playedThisRound.addAll(best.allPlayers());
            }

            // 🔥 연속 경기 카운트
            for (GamePlayer p : players) {
                if (playedThisRound.contains(p)) {
                    p.consecutiveRounds++;
                } else {
                    p.consecutiveRounds = 0;
                }
            }
        }

        return result;
    }

    private List<GamePlayer> createPlayers(int male, int female) {
        List<GamePlayer> list = new ArrayList<>();

        for (int i = 1; i <= male; i++) {
            list.add(new GamePlayer("M" + i, GamePlayer.Gender.MALE));
        }
        for (int i = 1; i <= female; i++) {
            list.add(new GamePlayer("F" + i, GamePlayer.Gender.FEMALE));
        }

        return list;
    }

    private void apply(MatchCandidate c, Map<Set<String>, Integer> groupCount) {

        List<GamePlayer> teamA = c.teamA;
        List<GamePlayer> teamB = c.teamB;

        // 🔥 경기 수 증가
        for (GamePlayer p : c.allPlayers()) {
            p.totalGames++;
        }

        // 🔥 파트너 기록
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

        // 🔥 상대 기록
        for (GamePlayer p1 : teamA) {
            for (GamePlayer p2 : teamB) {
                p1.opponentCount.merge(p2.id, 1, Integer::sum);
                p2.opponentCount.merge(p1.id, 1, Integer::sum);
            }
        }

        // 타입 경험 기록
        for (GamePlayer p : c.allPlayers()){
            p.typeExperience.merge(c.type, 1, Integer::sum);
        }

        // 🔥 4명 그룹 기록
        Set<String> group = c.allPlayers().stream()
                .map(p -> p.id)
                .collect(Collectors.toSet());

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

    private boolean isRandomType(MatchType t) {
        return t == MatchType.RANDOM_M3F1
                || t == MatchType.RANDOM_M1F3;
    }
}