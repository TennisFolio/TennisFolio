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
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class FixedScheduleGenerator {
    private static final EnumSet<MatchType> NORMAL_TYPES = EnumSet.of(
            MatchType.MIXED,
            MatchType.MALE,
            MatchType.FEMALE
    );
    private static final EnumSet<MatchType> SAME_GENDER_TYPES = EnumSet.of(
            MatchType.MALE,
            MatchType.FEMALE
    );

    private final ConstraintChecker constraintChecker;
    private final ScoreCalculator scoreCalculator;
    private final CandidateGenerator generator;
    private final SameGenderScheduleTargetCalculator sameGenderTargetCalculator = new SameGenderScheduleTargetCalculator();
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

    public ScheduleResult generateSchedule(int male, int female, int court, int totalGames, long seed) {
        int rounds = calculateRounds(totalGames, court);
        boolean allowRandom = shouldAllowRandomType(male, female, court, totalGames);
        EnumSet<MatchType> allowedMatchTypes = EnumSet.copyOf(NORMAL_TYPES);
        if (allowRandom) {
            allowedMatchTypes.add(MatchType.RANDOM_M3F1);
            allowedMatchTypes.add(MatchType.RANDOM_M1F3);
        }

        try {
            return generateSchedule(male, female, court, totalGames, rounds, seed, allowedMatchTypes);
        } catch (NoSuchElementException e) {
            if (allowRandom || !canScheduleRandomType(male, female)) {
                throw e;
            }

            EnumSet<MatchType> fallbackAllowedTypes = EnumSet.copyOf(NORMAL_TYPES);
            fallbackAllowedTypes.add(MatchType.RANDOM_M3F1);
            fallbackAllowedTypes.add(MatchType.RANDOM_M1F3);
            return generateSchedule(male, female, court, totalGames, rounds, seed, fallbackAllowedTypes);
        }
    }

    public ScheduleResult generateSchedule(
            int male,
            int female,
            int court,
            int totalGames,
            long seed,
            Set<MatchType> allowedMatchTypes
    ) {
        if (isSameGenderOnly(allowedMatchTypes)) {
            return generateSameGenderOnlySchedule(male, female, court, totalGames, seed);
        }

        int rounds = calculateRounds(totalGames, court);
        return generateSchedule(male, female, court, totalGames, rounds, seed, allowedMatchTypes);
    }

    private boolean isSameGenderOnly(Set<MatchType> allowedMatchTypes) {
        return allowedMatchTypes.size() == SAME_GENDER_TYPES.size()
                && allowedMatchTypes.containsAll(SAME_GENDER_TYPES);
    }

    private ScheduleResult generateSameGenderOnlySchedule(
            int male,
            int female,
            int court,
            int totalGames,
            long seed
    ) {
        List<SameGenderScheduleTarget> targets = sameGenderTargetCalculator.calculate(male, female, totalGames);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException(
                    "sameGenderDoublesOnly cannot allocate same-gender game counts for the requested player distribution"
            );
        }

        NoSuchElementException lastFailure = null;
        for (SameGenderScheduleTarget target : targets) {
            try {
                return generateSameGenderOnlySchedule(male, female, court, totalGames, seed, target);
            } catch (NoSuchElementException e) {
                lastFailure = e;
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new NoSuchElementException("No valid match candidate");
    }

    private ScheduleResult generateSameGenderOnlySchedule(
            int male,
            int female,
            int court,
            int totalGames,
            long seed,
            SameGenderScheduleTarget target
    ) {
        this.random = new Random(seed);
        List<GamePlayer> players = createPlayers(male, female);
        ScheduleResult result = new ScheduleResult();
        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
        for (MatchType type : MatchType.values()) {
            typeCount.put(type, 0);
        }
        Map<Set<String>, Integer> groupCount = new HashMap<>();
        int rounds = calculateRounds(totalGames, court);

        boolean solved = fillSameGenderOnly(
                players,
                result,
                typeCount,
                groupCount,
                target,
                court,
                totalGames,
                rounds,
                male,
                female,
                0
        );

        if (!solved) {
            throw new NoSuchElementException("No valid match candidate");
        }
        return result;
    }

    private boolean fillSameGenderOnly(
            List<GamePlayer> players,
            ScheduleResult result,
            Map<MatchType, Integer> typeCount,
            Map<Set<String>, Integer> groupCount,
            SameGenderScheduleTarget target,
            int court,
            int totalGames,
            int rounds,
            int male,
            int female,
            int gameIndex
    ) {
        if (gameIndex == totalGames) {
            return typeCount.get(MatchType.MALE) == target.maleGames()
                    && typeCount.get(MatchType.FEMALE) == target.femaleGames()
                    && players.stream().allMatch(player -> player.totalGames >= target.minGamesPerPlayer()
                            && player.totalGames <= target.maxGamesPerPlayer());
        }

        int round = gameIndex / court + 1;
        int courtNumber = gameIndex % court + 1;
        Set<GamePlayer> usedThisRound = usedPlayersInRound(result, round);
        List<GamePlayer> availablePlayers = players.stream()
                .filter(player -> !usedThisRound.contains(player))
                .toList();

        List<MatchCandidate> candidates = new ArrayList<>();
        generator.forEachCandidate(availablePlayers, SAME_GENDER_TYPES, candidate -> {
            if (typeCount.get(candidate.type) >= targetCount(target, candidate.type)) {
                return;
            }
            if (!isValidSameGenderCandidate(candidate, usedThisRound, target.maxGamesPerPlayer())) {
                return;
            }
            candidates.add(candidate);
        });

        Collections.shuffle(candidates, random);
        candidates.sort((left, right) -> Integer.compare(
                scoreSameGenderCandidate(right, typeCount, round, rounds, groupCount, male, female),
                scoreSameGenderCandidate(left, typeCount, round, rounds, groupCount, male, female)
        ));

        for (MatchCandidate candidate : candidates) {
            apply(candidate, groupCount);
            typeCount.put(candidate.type, typeCount.get(candidate.type) + 1);
            result.matches.add(new GameMatch(round, courtNumber, candidate.type, candidate.teamA, candidate.teamB));

            if (fillSameGenderOnly(players, result, typeCount, groupCount, target, court, totalGames, rounds, male, female, gameIndex + 1)) {
                return true;
            }

            result.matches.remove(result.matches.size() - 1);
            typeCount.put(candidate.type, typeCount.get(candidate.type) - 1);
            rollback(candidate, groupCount);
        }

        return false;
    }

    private boolean isValidSameGenderCandidate(
            MatchCandidate candidate,
            Set<GamePlayer> usedThisRound,
            int maxGames
    ) {
        List<GamePlayer> players = candidate.allPlayers();
        Set<GamePlayer> uniquePlayers = new HashSet<>(players);
        if (uniquePlayers.size() != players.size()) {
            return false;
        }

        for (GamePlayer player : players) {
            if (usedThisRound.contains(player)) {
                return false;
            }
            if (player.totalGames + 1 > maxGames) {
                return false;
            }
        }

        return true;
    }

    private Set<GamePlayer> usedPlayersInRound(ScheduleResult result, int round) {
        Set<GamePlayer> used = new HashSet<>();
        for (GameMatch match : result.matches) {
            if (match.round == round) {
                used.addAll(match.teamA);
                used.addAll(match.teamB);
            }
        }
        return used;
    }

    private int targetCount(SameGenderScheduleTarget target, MatchType type) {
        return switch (type) {
            case MALE -> target.maleGames();
            case FEMALE -> target.femaleGames();
            case MIXED, RANDOM_M3F1, RANDOM_M1F3 -> 0;
        };
    }

    private int scoreSameGenderCandidate(
            MatchCandidate candidate,
            Map<MatchType, Integer> typeCount,
            int round,
            int rounds,
            Map<Set<String>, Integer> groupCount,
            int male,
            int female
    ) {
        return scoreCalculator.score(
                candidate,
                typeCount,
                Set.of(),
                round,
                rounds,
                groupCount,
                male,
                female
        );
    }

    private ScheduleResult generateSchedule(
            int male,
            int female,
            int court,
            int totalGames,
            int rounds,
            long seed,
            Set<MatchType> allowedMatchTypes
    ) {
        this.random = new Random(seed);
        List<GamePlayer> players = createPlayers(male, female);

        int totalSlots = totalGames * 4;
        int maxGames = (int) Math.ceil((double) totalSlots / players.size());

        ScheduleResult result = new ScheduleResult();

        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);
        for (MatchType t : MatchType.values()) {
            typeCount.put(t, 0);
        }

        Map<Set<String>, Integer> groupCount = new HashMap<>();

        int remainingGames = totalGames;
        for (int r = 1; r <= rounds; r++) {
            Set<GamePlayer> used = new HashSet<>();
            Set<GamePlayer> playedThisRound = new HashSet<>();
            Set<MatchType> roundTypes = new HashSet<>();
            int gamesInRound = Math.min(court, remainingGames);

            for (int c = 1; c <= gamesInRound; c++) {
                List<GamePlayer> availablePlayers = players.stream()
                        .filter(p -> !used.contains(p))
                        .toList();

                BestCandidate best = selectBestCandidate(
                        availablePlayers,
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
                        female,
                        allowedMatchTypes
                );

                if (best.candidate == null) {
                    throw new NoSuchElementException("No valid match candidate");
                }

                apply(best.candidate, groupCount);

                typeCount.put(best.candidate.type, typeCount.get(best.candidate.type) + 1);
                roundTypes.add(best.candidate.type);

                result.matches.add(new GameMatch(r, c, best.candidate.type, best.candidate.teamA, best.candidate.teamB));
                remainingGames--;

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

    private int calculateRounds(int totalGames, int court) {
        return (int) Math.ceil((double) totalGames / court);
    }

    private BestCandidate selectBestCandidate(
            List<GamePlayer> availablePlayers,
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
            int female,
            Set<MatchType> allowedMatchTypes
    ) {
        BestCandidate best = new BestCandidate();

        generator.forEachCandidate(availablePlayers, allowedMatchTypes, candidate -> {
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

    private void rollback(MatchCandidate c, Map<Set<String>, Integer> groupCount) {
        List<GamePlayer> teamA = c.teamA;
        List<GamePlayer> teamB = c.teamB;
        List<GamePlayer> allPlayers = c.allPlayers();

        for (GamePlayer p : allPlayers) {
            p.totalGames--;
            p.typeExperience.merge(c.type, -1, Integer::sum);
            if (p.typeExperience.getOrDefault(c.type, 0) <= 0) {
                p.typeExperience.remove(c.type);
            }
        }

        for (GamePlayer p1 : teamA) {
            for (GamePlayer p2 : teamA) {
                if (p1 != p2) {
                    decrementCount(p1.partnerCount, p2.id);
                }
            }
        }

        for (GamePlayer p1 : teamB) {
            for (GamePlayer p2 : teamB) {
                if (p1 != p2) {
                    decrementCount(p1.partnerCount, p2.id);
                }
            }
        }

        for (GamePlayer p1 : teamA) {
            for (GamePlayer p2 : teamB) {
                decrementCount(p1.opponentCount, p2.id);
                decrementCount(p2.opponentCount, p1.id);
            }
        }

        Set<String> group = new HashSet<>(4);
        group.add(allPlayers.get(0).id);
        group.add(allPlayers.get(1).id);
        group.add(allPlayers.get(2).id);
        group.add(allPlayers.get(3).id);
        decrementCount(groupCount, group);
    }

    private <K> void decrementCount(Map<K, Integer> counts, K key) {
        int next = counts.getOrDefault(key, 0) - 1;
        if (next <= 0) {
            counts.remove(key);
        } else {
            counts.put(key, next);
        }
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
