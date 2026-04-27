package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CandidateGenerator {

    public List<MatchCandidate> generate(List<GamePlayer> players) {

        List<GamePlayer> men = players.stream()
                .filter(p -> p.gender == GamePlayer.Gender.MALE)
                .toList();

        List<GamePlayer> women = players.stream()
                .filter(p -> p.gender == GamePlayer.Gender.FEMALE)
                .toList();

        List<MatchCandidate> result = new ArrayList<>();

        result.addAll(generateMixed(men, women));
        result.addAll(generateMale(men));
        result.addAll(generateFemale(women));
        result.addAll(generateRandomM3F1(men, women));
        result.addAll(generateRandomM1F3(men, women));

        return result;
    }

    // 🔥 혼복 (남2 여2)
    private List<MatchCandidate> generateMixed(List<GamePlayer> men, List<GamePlayer> women) {

        List<MatchCandidate> result = new ArrayList<>();

        List<List<GamePlayer>> menComb = combinations(men, 2);
        List<List<GamePlayer>> womenComb = combinations(women, 2);

        for (List<GamePlayer> m : menComb) {
            for (List<GamePlayer> w : womenComb) {

                // 팀 조합 2가지
                result.add(new MatchCandidate(
                        MatchType.MIXED,
                        List.of(m.get(0), w.get(0)),
                        List.of(m.get(1), w.get(1))
                ));

                result.add(new MatchCandidate(
                        MatchType.MIXED,
                        List.of(m.get(0), w.get(1)),
                        List.of(m.get(1), w.get(0))
                ));
            }
        }

        return result;
    }

    // 🔥 남복 (남4)
    private List<MatchCandidate> generateMale(List<GamePlayer> men) {

        List<MatchCandidate> result = new ArrayList<>();

        List<List<GamePlayer>> comb = combinations(men, 4);

        for (List<GamePlayer> p : comb) {

            result.add(new MatchCandidate(
                    MatchType.MALE,
                    List.of(p.get(0), p.get(1)),
                    List.of(p.get(2), p.get(3))
            ));

            result.add(new MatchCandidate(
                    MatchType.MALE,
                    List.of(p.get(0), p.get(2)),
                    List.of(p.get(1), p.get(3))
            ));

            result.add(new MatchCandidate(
                    MatchType.MALE,
                    List.of(p.get(0), p.get(3)),
                    List.of(p.get(1), p.get(2))
            ));
        }

        return result;
    }

    // 🔥 여복 (여4)
    private List<MatchCandidate> generateFemale(List<GamePlayer> women) {

        List<MatchCandidate> result = new ArrayList<>();

        List<List<GamePlayer>> comb = combinations(women, 4);

        for (List<GamePlayer> p : comb) {

            result.add(new MatchCandidate(
                    MatchType.FEMALE,
                    List.of(p.get(0), p.get(1)),
                    List.of(p.get(2), p.get(3))
            ));

            result.add(new MatchCandidate(
                    MatchType.FEMALE,
                    List.of(p.get(0), p.get(2)),
                    List.of(p.get(1), p.get(3))
            ));

            result.add(new MatchCandidate(
                    MatchType.FEMALE,
                    List.of(p.get(0), p.get(3)),
                    List.of(p.get(1), p.get(2))
            ));
        }

        return result;
    }

    // 🔥 잡복 (남3 여1)
    private List<MatchCandidate> generateRandomM3F1(List<GamePlayer> men, List<GamePlayer> women) {

        List<MatchCandidate> result = new ArrayList<>();

        List<List<GamePlayer>> menComb = combinations(men, 3);

        for (List<GamePlayer> m : menComb) {
            for (GamePlayer f : women) {

                result.add(new MatchCandidate(
                        MatchType.RANDOM_M3F1,
                        List.of(m.get(0), m.get(1)),
                        List.of(m.get(2), f)
                ));

                result.add(new MatchCandidate(
                        MatchType.RANDOM_M3F1,
                        List.of(m.get(0), f),
                        List.of(m.get(1), m.get(2))
                ));
            }
        }

        return result;
    }

    // 🔥 잡복 (남1 여3)
    private List<MatchCandidate> generateRandomM1F3(List<GamePlayer> men, List<GamePlayer> women) {

        List<MatchCandidate> result = new ArrayList<>();

        List<List<GamePlayer>> womenComb = combinations(women, 3);

        for (GamePlayer m : men) {
            for (List<GamePlayer> w : womenComb) {

                result.add(new MatchCandidate(
                        MatchType.RANDOM_M1F3,
                        List.of(m, w.get(0)),
                        List.of(w.get(1), w.get(2))
                ));

                result.add(new MatchCandidate(
                        MatchType.RANDOM_M1F3,
                        List.of(w.get(0), w.get(1)),
                        List.of(m, w.get(2))
                ));
            }
        }

        return result;
    }

    // 🔥 조합 생성 (핵심 유틸)
    private <T> List<List<T>> combinations(List<T> list, int size) {

        List<List<T>> result = new ArrayList<>();
        dfs(list, size, 0, new ArrayList<>(), result);
        return result;
    }

    private <T> void dfs(
            List<T> list,
            int size,
            int idx,
            List<T> current,
            List<List<T>> result
    ) {

        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = idx; i < list.size(); i++) {
            current.add(list.get(i));
            dfs(list, size, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}
