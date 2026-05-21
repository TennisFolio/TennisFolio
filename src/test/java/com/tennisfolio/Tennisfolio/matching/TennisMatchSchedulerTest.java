package com.tennisfolio.Tennisfolio.matching;

import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.engine.CandidateGenerator;
import com.tennisfolio.Tennisfolio.matching.engine.ConstraintChecker;
import com.tennisfolio.Tennisfolio.matching.engine.ScoreCalculator;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.service.TennisMatchScheduler;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.entry;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.game;
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

    @Test
    void generateSchedule_placesEveryCourtInEveryRoundWithoutPlayerOverlapPerRound() {
        TennisMatchScheduler scheduler = createScheduler();

        int male = 8;
        int female = 8;
        int court = 3;
        int rounds = 5;

        ScheduleResult result = scheduler.generateSchedule(male, female, court, rounds, 136);

        assertEquals(court * rounds, result.matches.size());

        for (int round = 1; round <= rounds; round++) {
            int currentRound = round;
            List<GameMatch> roundMatches = result.matches.stream()
                    .filter(match -> match.round == currentRound)
                    .toList();

            assertEquals(court, roundMatches.size());
            assertEquals(
                    Set.of(1, 2, 3),
                    roundMatches.stream().map(match -> match.court).collect(Collectors.toSet())
            );

            Set<String> playersInRound = new HashSet<>();
            for (GameMatch match : roundMatches) {
                for (GamePlayer player : allPlayers(match)) {
                    assertTrue(playersInRound.add(player.id), "Player appears more than once in round " + currentRound);
                }
            }
        }
    }

    @Test
    void generateSchedule_createsTeamsMatchingDeclaredMatchType() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult result = scheduler.generateSchedule(7, 9, 3, 4, 136);

        for (GameMatch match : result.matches) {
            assertEquals(2, match.teamA.size());
            assertEquals(2, match.teamB.size());
            assertEquals(4, allPlayerIds(match).size());

            long men = allPlayers(match).stream()
                    .filter(player -> player.gender == GamePlayer.Gender.MALE)
                    .count();
            long women = allPlayers(match).stream()
                    .filter(player -> player.gender == GamePlayer.Gender.FEMALE)
                    .count();

            switch (match.type) {
                case MIXED -> {
                    assertEquals(2, men);
                    assertEquals(2, women);
                }
                case MALE -> {
                    assertEquals(4, men);
                    assertEquals(0, women);
                }
                case FEMALE -> {
                    assertEquals(0, men);
                    assertEquals(4, women);
                }
                case RANDOM_M3F1 -> {
                    assertEquals(3, men);
                    assertEquals(1, women);
                }
                case RANDOM_M1F3 -> {
                    assertEquals(1, men);
                    assertEquals(3, women);
                }
            }
        }
    }

    @Test
    void generateSchedule_returnsSameScheduleForSameSeed() {
        TennisMatchScheduler scheduler = createScheduler();

        ScheduleResult first = scheduler.generateSchedule(8, 8, 3, 5, 136);
        ScheduleResult second = scheduler.generateSchedule(8, 8, 3, 5, 136);

        assertEquals(scheduleSignature(first), scheduleSignature(second));
    }

    @Test
    void generateNextClubSessionGame_usesCandidateEntryIdsAndRequestedCourtRound() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "F1", CompetitionEntry.Gender.FEMALE),
                entry(4L, competition, "F2", CompetitionEntry.Gender.FEMALE)
        );

        GameMatch match = scheduler.generateNextClubSessionGame(entries, List.of(), 2, 7, 136L);

        assertEquals(2, match.court);
        assertEquals(7, match.round);
        assertEquals(MatchType.MIXED, match.type);
        assertEquals(Set.of("1", "2", "3", "4"), allPlayerIds(match));
        assertEquals(2, match.teamA.size());
        assertEquals(2, match.teamB.size());
    }

    @Test
    void generateNextClubSessionGame_doesNotUseRandomTypeWhenNormalTypeIsAvailable() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "M3", CompetitionEntry.Gender.MALE),
                entry(4L, competition, "M4", CompetitionEntry.Gender.MALE),
                entry(5L, competition, "F1", CompetitionEntry.Gender.FEMALE),
                entry(6L, competition, "F2", CompetitionEntry.Gender.FEMALE),
                entry(7L, competition, "F3", CompetitionEntry.Gender.FEMALE),
                entry(8L, competition, "F4", CompetitionEntry.Gender.FEMALE)
        );

        for (long seed = 1; seed <= 20; seed++) {
            GameMatch match = scheduler.generateNextClubSessionGame(entries, List.of(), 1, 1, seed);

            assertNotEquals(MatchType.RANDOM_M3F1, match.type);
            assertNotEquals(MatchType.RANDOM_M1F3, match.type);
        }
    }

    @Test
    void generateNextClubSessionGame_appliesCompletedGameHistoryToAvoidRepeatedGroups() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "M3", CompetitionEntry.Gender.MALE),
                entry(4L, competition, "F1", CompetitionEntry.Gender.FEMALE),
                entry(5L, competition, "F2", CompetitionEntry.Gender.FEMALE),
                entry(6L, competition, "F3", CompetitionEntry.Gender.FEMALE)
        );
        Game previousGame = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        List<GameEntry> history = List.of(
                new GameEntry(previousGame, entries.get(0), GameEntry.Team.A, 1),
                new GameEntry(previousGame, entries.get(3), GameEntry.Team.A, 2),
                new GameEntry(previousGame, entries.get(1), GameEntry.Team.B, 1),
                new GameEntry(previousGame, entries.get(4), GameEntry.Team.B, 2)
        );

        GameMatch match = scheduler.generateNextClubSessionGame(entries, history, 1, 2, 136L);

        Set<String> previousGroup = history.stream()
                .map(gameEntry -> String.valueOf(gameEntry.getCompetitionEntry().getId()))
                .collect(Collectors.toSet());
        assertEquals(4, allPlayerIds(match).size());
        assertNotEquals(previousGroup, allPlayerIds(match));
    }

    @Test
    void generateNextClubSessionGame_avoidsThirdConsecutiveGameWhenPossible() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "M3", CompetitionEntry.Gender.MALE),
                entry(4L, competition, "M4", CompetitionEntry.Gender.MALE),
                entry(5L, competition, "M5", CompetitionEntry.Gender.MALE),
                entry(6L, competition, "M6", CompetitionEntry.Gender.MALE),
                entry(7L, competition, "M7", CompetitionEntry.Gender.MALE),
                entry(8L, competition, "M8", CompetitionEntry.Gender.MALE)
        );
        List<GameEntry> history = List.of(
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(0), GameEntry.Team.A, 1),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(1), GameEntry.Team.A, 2),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(2), GameEntry.Team.B, 1),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(3), GameEntry.Team.B, 2),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(0), GameEntry.Team.A, 1),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(1), GameEntry.Team.A, 2),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(2), GameEntry.Team.B, 1),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(3), GameEntry.Team.B, 2)
        );

        GameMatch match = scheduler.generateNextClubSessionGame(entries, history, 1, 3, 136L);

        assertEquals(Set.of("5", "6", "7", "8"), allPlayerIds(match));
    }

    @Test
    void generateNextClubSessionGame_allowsThirdConsecutiveGameWhenNoAlternativeExists() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "M3", CompetitionEntry.Gender.MALE),
                entry(4L, competition, "M4", CompetitionEntry.Gender.MALE)
        );
        List<GameEntry> history = List.of(
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(0), GameEntry.Team.A, 1),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(1), GameEntry.Team.A, 2),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(2), GameEntry.Team.B, 1),
                new GameEntry(game(10L, competition, 1, 1, Game.MatchType.MALE), entries.get(3), GameEntry.Team.B, 2),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(0), GameEntry.Team.A, 1),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(1), GameEntry.Team.A, 2),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(2), GameEntry.Team.B, 1),
                new GameEntry(game(11L, competition, 2, 1, Game.MatchType.MALE), entries.get(3), GameEntry.Team.B, 2)
        );

        GameMatch match = scheduler.generateNextClubSessionGame(entries, history, 1, 3, 136L);

        assertEquals(Set.of("1", "2", "3", "4"), allPlayerIds(match));
    }

    @Test
    void generateNextClubSessionGame_throwsWhenNoCandidateCanMakeGame() {
        TennisMatchScheduler scheduler = createScheduler();
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "F1", CompetitionEntry.Gender.FEMALE)
        );

        assertThrows(
                NoSuchElementException.class,
                () -> scheduler.generateNextClubSessionGame(entries, List.of(), 1, 1, 136L)
        );
    }

    private TennisMatchScheduler createScheduler() {
        return new TennisMatchScheduler(
                new ConstraintChecker(),
                new ScoreCalculator(),
                new CandidateGenerator()
        );
    }

    private List<GamePlayer> allPlayers(GameMatch match) {
        List<GamePlayer> players = new ArrayList<>();
        players.addAll(match.teamA);
        players.addAll(match.teamB);
        return players;
    }

    private Set<String> allPlayerIds(GameMatch match) {
        return allPlayers(match).stream()
                .map(player -> player.id)
                .collect(Collectors.toSet());
    }

    private List<String> scheduleSignature(ScheduleResult result) {
        return result.matches.stream()
                .map(match -> match.round + ":" + match.court + ":" + match.type + ":"
                        + teamSignature(match.teamA) + ":" + teamSignature(match.teamB))
                .toList();
    }

    private String teamSignature(List<GamePlayer> team) {
        return team.stream()
                .map(player -> player.id)
                .collect(Collectors.joining(","));
    }

}
