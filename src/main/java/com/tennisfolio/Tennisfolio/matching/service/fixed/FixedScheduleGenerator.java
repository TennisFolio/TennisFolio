package com.tennisfolio.Tennisfolio.matching.service.fixed;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ConstraintChecker;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class FixedScheduleGenerator {

    private final ConstraintChecker constraintChecker;
    private final ScoreCalculator scoreCalculator;
    private final CandidateGenerator generator;
    private Random random;

    public FixedScheduleGenerator(
            ConstraintChecker constraintChecker,
            ScoreCalculator scoreCalculator,
            CandidateGenerator generator
    ) {
        this.constraintChecker = constraintChecker;
        this.scoreCalculator = scoreCalculator;
        this.generator = generator;
    }

    public ScheduleResult generateSchedule(int male, int female, int court, int rounds, long seed) {
        int totalMatches = court * rounds;
        boolean allowRandom = shouldAllowRandomType(male, female, court, totalMatches);

        try {
            return generateSchedule(male, female, court, rounds, seed, allowRandom);
        } catch (NoSuchElementException e) {
            if (allowRandom || !canScheduleRandomType(male, female)) {
                throw e;
            }

            return generateSchedule(male, female, court, rounds, seed, true);
        }
    }

    private ScheduleResult generateSchedule(int male, int female, int court, int rounds, long seed, boolean allowRandom) {
        this.random = new Random(seed);
        List<GamePlayer> players = createPlayers(male, female);

        int totalMatches = court * rounds;
        int totalSlots = totalMatches * 4;
        int maxGames = (int) Math.ceil((double) totalSlots / players.size());

        ScheduleResult result = new ScheduleResult();

        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
        for (MatchType t : MatchType.values()) {
            typeCount.put(t, 0);
        }

        Map<Set<String>, Integer> groupCount = new HashMap<>();

        for (int r = 1; r <= rounds; r++) {
            Set<GamePlayer> used = new HashSet<>();
            Set<GamePlayer> playedThisRound = new HashSet<>();
            Set<MatchType> roundTypes = new HashSet<>();

            for (int c = 1; c <= court; c++) {
                List<GamePlayer> availablePlayers = players.stream()
                        .filter(p -> !used.contains(p))
                        .toList();

                BestCandidate best = selectBestCandidate(
                        availablePlayers,
                        allowRandom,
                        players,
                        used,
                        court,
                        maxGames,
                        typeCount,
                        roundTypes,
                        r,
                        rounds,
                        groupCount,
                        male,
                        female
                );

                if (best.candidate == null) {
                    throw new NoSuchElementException("No valid match candidate");
                }

                apply(best.candidate, groupCount);

                typeCount.put(best.candidate.type, typeCount.get(best.candidate.type) + 1);
                roundTypes.add(best.candidate.type);

                result.matches.add(new GameMatch(r, c, best.candidate.type, best.candidate.teamA, best.candidate.teamB));

                used.addAll(best.candidate.allPlayers());
                playedThisRound.addAll(best.candidate.allPlayers());
            }

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

    private BestCandidate selectBestCandidate(
            List<GamePlayer> availablePlayers,
            boolean allowRandom,
            List<GamePlayer> players,
            Set<GamePlayer> used,
            int court,
            int maxGames,
            Map<MatchType, Integer> typeCount,
            Set<MatchType> roundTypes,
            int currentRound,
            int rounds,
            Map<Set<String>, Integer> groupCount,
            int male,
            int female
    ) {
        BestCandidate best = new BestCandidate();

        generator.forEachCandidate(availablePlayers, allowRandom, candidate -> {
            if (!constraintChecker.isValid(candidate, players, used, court, maxGames)) {
                return;
            }

            int score = scoreCalculator.score(
                    candidate,
                    typeCount,
                    roundTypes,
                    currentRound,
                    rounds,
                    groupCount,
                    male,
                    female
            );
            int tieBreaker = random.nextInt();

            if (best.candidate == null || score > best.score || (score == best.score && tieBreaker > best.tieBreaker)) {
                best.candidate = candidate;
                best.score = score;
                best.tieBreaker = tieBreaker;
            }
        });

        return best;
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

    private boolean shouldAllowRandomType(int male, int female, int court, int totalMatches) {
        if (!canScheduleRandomType(male, female)) {
            return false;
        }

        if (!canScheduleWithoutRandom(male, female, court)) {
            return true;
        }

        return !canFillGenderSlotsWithoutRandom(male, female, totalMatches);
    }

    private boolean canFillGenderSlotsWithoutRandom(int male, int female, int totalMatches) {
        int totalSlots = totalMatches * 4;
        int playerCount = male + female;
        int minGames = totalSlots / playerCount;
        int extraGames = totalSlots % playerCount;

        for (int maleExtraGames = 0; maleExtraGames <= extraGames; maleExtraGames++) {
            int femaleExtraGames = extraGames - maleExtraGames;
            if (maleExtraGames > male || femaleExtraGames > female) {
                continue;
            }

            int maleSlots = male * minGames + maleExtraGames;
            if (canFillMaleSlotsWithNormalTypes(maleSlots, totalMatches, male, female)) {
                return true;
            }
        }

        return false;
    }

    private boolean canFillMaleSlotsWithNormalTypes(int maleSlots, int totalMatches, int male, int female) {
        if (maleSlots < 0 || maleSlots > totalMatches * 4) {
            return false;
        }

        for (int mixed = 0; mixed <= totalMatches; mixed++) {
            if (mixed > 0 && (male < 2 || female < 2)) {
                continue;
            }

            int remainingMatches = totalMatches - mixed;
            for (int maleMatch = 0; maleMatch <= remainingMatches; maleMatch++) {
                int femaleMatch = remainingMatches - maleMatch;

                if (maleMatch > 0 && male < 4) {
                    continue;
                }
                if (femaleMatch > 0 && female < 4) {
                    continue;
                }

                int normalMaleSlots = mixed * 2 + maleMatch * 4;
                if (normalMaleSlots == maleSlots) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canScheduleRandomType(int male, int female) {
        return (male >= 3 && female >= 1) || (male >= 1 && female >= 3);
    }

    private static class BestCandidate {
        private MatchCandidate candidate;
        private int score = Integer.MIN_VALUE;
        private int tieBreaker = Integer.MIN_VALUE;
    }
}
