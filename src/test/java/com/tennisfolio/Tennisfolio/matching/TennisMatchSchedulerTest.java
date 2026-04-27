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

        TennisMatchScheduler scheduler = new TennisMatchScheduler(checker, calculator, generator, 136);

        int male = 6;
        int female = 6;
        int court = 2;
        int rounds = 6;

        ScheduleResult result = scheduler.generate(male, female, court, rounds);

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
}