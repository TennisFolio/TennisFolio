package com.tennisfolio.Tennisfolio.matching;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ConstraintChecker;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;
import com.tennisfolio.Tennisfolio.matching.service.TennisMatchScheduler;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TennisMatchSchedulerTest {

    @Test
    void test_basic() {

        ConstraintChecker checker = new ConstraintChecker();
        ScoreCalculator calculator = new ScoreCalculator();
        CandidateGenerator generator = new CandidateGenerator();

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator);

        int male = 15;
        int female = 25;
        int court = 10;
        int rounds = 20;

        ScheduleResult result = scheduler.generateSchedule(male, female, court, rounds, 136);

        // 🔥 1. 경기 출력
        System.out.println("\n=== 경기 스케줄 ===");

        for (GameMatch match : result.matches) {
            System.out.println(
                    "[Round " + match.round + " / Court " + match.court + "] "
                            + match.type + " : "
                            + match.teamA + " vs " + match.teamB
            );
        }

        // 🔥 2. 플레이어별 경기 수 집계
        Map<String, Integer> gameCount = new HashMap<>();

        // 🔥 3. 타입별 경기 수 집계
        Map<MatchType, Integer> typeCount = new EnumMap<>(MatchType.class);

        for (MatchType type : MatchType.values()) {
            typeCount.put(type, 0);
        }

        for (GameMatch match : result.matches) {

            typeCount.put(match.type, typeCount.get(match.type) + 1);

            for (GamePlayer p : match.teamA) {
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            }
            for (GamePlayer p : match.teamB) {
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            }
        }

        // 🔥 4. 플레이어별 출력
        System.out.println("\n=== 플레이어별 경기 수 ===");

        gameCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e ->
                        System.out.println(e.getKey() + " : " + e.getValue())
                );

        // 🔥 5. 타입별 출력
        System.out.println("\n=== 타입별 경기 수 ===");

        typeCount.forEach((k, v) ->
                System.out.println(k + " : " + v)
        );

        // 🔥 6. 공정성 체크
        int max = Collections.max(gameCount.values());
        int min = Collections.min(gameCount.values());

        System.out.println("\n=== 공정성 ===");
        System.out.println("max=" + max + ", min=" + min);

        assertTrue(max - min <= 1, "경기 수 편차가 1을 초과함");

        // 🔥 7. 경기당 4명 체크
        for (GameMatch match : result.matches) {
            Set<String> players = new HashSet<>();

            match.teamA.forEach(p -> players.add(p.id));
            match.teamB.forEach(p -> players.add(p.id));

            assertEquals(4, players.size(), "중복 플레이어 존재");
        }
    }

    @Test
    void generateSchedule_usesRandomTypeWhenRestingPlayersHaveThreeToOneGenderSplit() {
        ConstraintChecker checker = new ConstraintChecker();
        ScoreCalculator calculator = new ScoreCalculator();
        CandidateGenerator generator = new CandidateGenerator();

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator);

        ScheduleResult result = scheduler.generateSchedule(7, 9, 3, 4, 136);

        assertEquals(12, result.matches.size());
        assertTrue(result.matches.stream()
                .anyMatch(match -> match.type == MatchType.RANDOM_M1F3 || match.type == MatchType.RANDOM_M3F1));

        Map<String, Integer> gameCount = new HashMap<>();

        for (GameMatch match : result.matches) {
            Set<String> players = new HashSet<>();

            match.teamA.forEach(p -> {
                players.add(p.id);
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            });
            match.teamB.forEach(p -> {
                players.add(p.id);
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            });

            assertEquals(4, players.size());
        }

        assertEquals(16, gameCount.size());
        assertEquals(3, Collections.max(gameCount.values()));
        assertEquals(3, Collections.min(gameCount.values()));
    }

    @Test
    void generateSchedule_usesRandomTypeWhenOnlyOnePlayerExistsInOneGender() {
        ConstraintChecker checker = new ConstraintChecker();
        ScoreCalculator calculator = new ScoreCalculator();
        CandidateGenerator generator = new CandidateGenerator();

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator);

        ScheduleResult result = scheduler.generateSchedule(1, 4, 1, 4, 136);

        assertEquals(4, result.matches.size());
        assertTrue(result.matches.stream()
                .anyMatch(match -> match.type == MatchType.RANDOM_M1F3));

        Map<String, Integer> gameCount = new HashMap<>();

        for (GameMatch match : result.matches) {
            Set<String> players = new HashSet<>();

            match.teamA.forEach(p -> {
                players.add(p.id);
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            });
            match.teamB.forEach(p -> {
                players.add(p.id);
                gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
            });

            assertEquals(4, players.size());
        }

        assertEquals(5, gameCount.size());
        assertTrue(Collections.max(gameCount.values()) - Collections.min(gameCount.values()) <= 1);
    }

    @Test
    void generateSchedule_usesRandomTypeWhenOneGenderCannotMakeSameGenderMatch() {
        ConstraintChecker checker = new ConstraintChecker();
        ScoreCalculator calculator = new ScoreCalculator();
        CandidateGenerator generator = new CandidateGenerator();

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator);

        int[][] cases = {
                {2, 3, 1},
                {3, 6, 2},
                {3, 7, 2},
                {6, 3, 2},
                {7, 3, 2}
        };

        for (int[] testCase : cases) {
            int male = testCase[0];
            int female = testCase[1];
            int court = testCase[2];
            ScheduleResult result = scheduler.generateSchedule(male, female, court, 4, 136);

            assertEquals(court * 4, result.matches.size());
            assertTrue(result.matches.stream()
                    .anyMatch(match -> match.type == MatchType.RANDOM_M1F3 || match.type == MatchType.RANDOM_M3F1));

            Map<String, Integer> gameCount = new HashMap<>();

            for (GameMatch match : result.matches) {
                Set<String> players = new HashSet<>();

                match.teamA.forEach(p -> {
                    players.add(p.id);
                    gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
                });
                match.teamB.forEach(p -> {
                    players.add(p.id);
                    gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
                });

                assertEquals(4, players.size());
            }

            assertEquals(male + female, gameCount.size());
            assertTrue(Collections.max(gameCount.values()) - Collections.min(gameCount.values()) <= 1);
        }
    }

    @Test
    void generateSchedule_usesRandomTypeWhenNormalTypesCannotFillOverallGenderSlots() {
        ConstraintChecker checker = new ConstraintChecker();
        ScoreCalculator calculator = new ScoreCalculator();
        CandidateGenerator generator = new CandidateGenerator();

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator);

        int[][] cases = {
                {5, 7, 1},
                {7, 5, 1},
                {7, 9, 1},
                {7, 9, 2},
                {9, 7, 1},
                {9, 7, 2}
        };

        for (int[] testCase : cases) {
            int male = testCase[0];
            int female = testCase[1];
            int court = testCase[2];
            ScheduleResult result = scheduler.generateSchedule(male, female, court, 4, 136);

            assertEquals(court * 4, result.matches.size());
            assertTrue(result.matches.stream()
                    .anyMatch(match -> match.type == MatchType.RANDOM_M1F3 || match.type == MatchType.RANDOM_M3F1));

            Map<String, Integer> gameCount = new HashMap<>();

            for (GameMatch match : result.matches) {
                Set<String> players = new HashSet<>();

                match.teamA.forEach(p -> {
                    players.add(p.id);
                    gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
                });
                match.teamB.forEach(p -> {
                    players.add(p.id);
                    gameCount.put(p.id, gameCount.getOrDefault(p.id, 0) + 1);
                });

                assertEquals(4, players.size());
            }

            assertEquals(male + female, gameCount.size());
            assertTrue(Collections.max(gameCount.values()) - Collections.min(gameCount.values()) <= 1);
        }
    }
}
