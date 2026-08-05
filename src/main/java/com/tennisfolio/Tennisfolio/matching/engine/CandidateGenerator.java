package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class CandidateGenerator {
    private static final EnumSet<MatchType> NORMAL_TYPES = EnumSet.of(
            MatchType.MIXED,
            MatchType.MALE,
            MatchType.FEMALE
    );

    private static final EnumSet<MatchType> RANDOM_TYPES = EnumSet.of(
            MatchType.RANDOM_M3F1,
            MatchType.RANDOM_M1F3
    );

    public List<MatchCandidate> generate(List<GamePlayer> players, boolean allowRandom) {
        List<MatchCandidate> result = new ArrayList<>();
        forEachCandidate(players, allowRandom, result::add);
        return result;
    }

    public List<MatchCandidate> generate(List<GamePlayer> players, Set<MatchType> allowedMatchTypes) {
        List<MatchCandidate> result = new ArrayList<>();
        forEachCandidate(players, allowedMatchTypes, result::add);
        return result;
    }

    public void forEachCandidate(List<GamePlayer> players, boolean allowRandom, Consumer<MatchCandidate> consumer) {
        EnumSet<MatchType> allowedMatchTypes = EnumSet.copyOf(NORMAL_TYPES);
        if (allowRandom) {
            allowedMatchTypes.addAll(RANDOM_TYPES);
        }
        forEachCandidate(players, allowedMatchTypes, consumer);
    }

    public void forEachCandidate(
            List<GamePlayer> players,
            Set<MatchType> allowedMatchTypes,
            Consumer<MatchCandidate> consumer
    ) {
        List<GamePlayer> men = new ArrayList<>();
        List<GamePlayer> women = new ArrayList<>();

        for (GamePlayer player : players) {
            if (player.gender == GamePlayer.Gender.MALE) {
                men.add(player);
            } else {
                women.add(player);
            }
        }

        if (allowedMatchTypes.contains(MatchType.MIXED)) {
            generateMixed(men, women, consumer);
        }
        if (allowedMatchTypes.contains(MatchType.MALE)) {
            generateMale(men, consumer);
        }
        if (allowedMatchTypes.contains(MatchType.FEMALE)) {
            generateFemale(women, consumer);
        }
        if (allowedMatchTypes.contains(MatchType.RANDOM_M3F1)) {
            generateRandomM3F1(men, women, consumer);
        }
        if (allowedMatchTypes.contains(MatchType.RANDOM_M1F3)) {
            generateRandomM1F3(men, women, consumer);
        }
    }

    public List<MatchCandidate> generateSkillBalancedCandidates(List<GamePlayer> players) {
        List<MatchCandidate> candidates = new ArrayList<>();
        forEachSkillBalancedCandidate(players, candidates::add);
        return candidates;
    }

    public void forEachSkillBalancedCandidate(List<GamePlayer> players, Consumer<MatchCandidate> consumer) {
        for (int first = 0; first < players.size() - 3; first++) {
            for (int second = first + 1; second < players.size() - 2; second++) {
                for (int third = second + 1; third < players.size() - 1; third++) {
                    for (int fourth = third + 1; fourth < players.size(); fourth++) {
                        GamePlayer player1 = players.get(first);
                        GamePlayer player2 = players.get(second);
                        GamePlayer player3 = players.get(third);
                        GamePlayer player4 = players.get(fourth);

                        emitSkillBalancedCandidate(player1, player2, player3, player4, consumer);
                        emitSkillBalancedCandidate(player1, player3, player2, player4, consumer);
                        emitSkillBalancedCandidate(player1, player4, player2, player3, consumer);
                    }
                }
            }
        }
    }

    private void emitSkillBalancedCandidate(
            GamePlayer first,
            GamePlayer second,
            GamePlayer third,
            GamePlayer fourth,
            Consumer<MatchCandidate> consumer
    ) {
        List<GamePlayer> teamA = List.of(first, second);
        List<GamePlayer> teamB = List.of(third, fourth);
        consumer.accept(new MatchCandidate(determineMatchType(teamA, teamB), teamA, teamB));
    }

    private MatchType determineMatchType(List<GamePlayer> teamA, List<GamePlayer> teamB) {
        long maleCount = List.of(teamA, teamB).stream()
                .flatMap(List::stream)
                .filter(player -> player.gender == GamePlayer.Gender.MALE)
                .count();

        return switch ((int) maleCount) {
            case 4 -> MatchType.MALE;
            case 0 -> MatchType.FEMALE;
            case 3 -> MatchType.RANDOM_M3F1;
            case 1 -> MatchType.RANDOM_M1F3;
            case 2 -> isMixedTeam(teamA) && isMixedTeam(teamB) ? MatchType.MIXED : MatchType.M2F2_SPLIT;
            default -> throw new IllegalStateException("A doubles candidate must contain four players");
        };
    }

    private boolean isMixedTeam(List<GamePlayer> team) {
        return team.get(0).gender != team.get(1).gender;
    }

    private void generateMixed(List<GamePlayer> men, List<GamePlayer> women, Consumer<MatchCandidate> consumer) {
        for (int m1 = 0; m1 < men.size() - 1; m1++) {
            for (int m2 = m1 + 1; m2 < men.size(); m2++) {
                for (int w1 = 0; w1 < women.size() - 1; w1++) {
                    for (int w2 = w1 + 1; w2 < women.size(); w2++) {
                        GamePlayer man1 = men.get(m1);
                        GamePlayer man2 = men.get(m2);
                        GamePlayer woman1 = women.get(w1);
                        GamePlayer woman2 = women.get(w2);

                        consumer.accept(new MatchCandidate(
                                MatchType.MIXED,
                                List.of(man1, woman1),
                                List.of(man2, woman2)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.MIXED,
                                List.of(man1, woman2),
                                List.of(man2, woman1)
                        ));
                    }
                }
            }
        }
    }

    private void generateMale(List<GamePlayer> men, Consumer<MatchCandidate> consumer) {
        for (int p1 = 0; p1 < men.size() - 3; p1++) {
            for (int p2 = p1 + 1; p2 < men.size() - 2; p2++) {
                for (int p3 = p2 + 1; p3 < men.size() - 1; p3++) {
                    for (int p4 = p3 + 1; p4 < men.size(); p4++) {
                        GamePlayer player1 = men.get(p1);
                        GamePlayer player2 = men.get(p2);
                        GamePlayer player3 = men.get(p3);
                        GamePlayer player4 = men.get(p4);

                        consumer.accept(new MatchCandidate(
                                MatchType.MALE,
                                List.of(player1, player2),
                                List.of(player3, player4)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.MALE,
                                List.of(player1, player3),
                                List.of(player2, player4)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.MALE,
                                List.of(player1, player4),
                                List.of(player2, player3)
                        ));
                    }
                }
            }
        }
    }

    private void generateFemale(List<GamePlayer> women, Consumer<MatchCandidate> consumer) {
        for (int p1 = 0; p1 < women.size() - 3; p1++) {
            for (int p2 = p1 + 1; p2 < women.size() - 2; p2++) {
                for (int p3 = p2 + 1; p3 < women.size() - 1; p3++) {
                    for (int p4 = p3 + 1; p4 < women.size(); p4++) {
                        GamePlayer player1 = women.get(p1);
                        GamePlayer player2 = women.get(p2);
                        GamePlayer player3 = women.get(p3);
                        GamePlayer player4 = women.get(p4);

                        consumer.accept(new MatchCandidate(
                                MatchType.FEMALE,
                                List.of(player1, player2),
                                List.of(player3, player4)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.FEMALE,
                                List.of(player1, player3),
                                List.of(player2, player4)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.FEMALE,
                                List.of(player1, player4),
                                List.of(player2, player3)
                        ));
                    }
                }
            }
        }
    }

    private void generateRandomM3F1(List<GamePlayer> men, List<GamePlayer> women, Consumer<MatchCandidate> consumer) {
        for (int m1 = 0; m1 < men.size() - 2; m1++) {
            for (int m2 = m1 + 1; m2 < men.size() - 1; m2++) {
                for (int m3 = m2 + 1; m3 < men.size(); m3++) {
                    for (GamePlayer woman : women) {
                        GamePlayer man1 = men.get(m1);
                        GamePlayer man2 = men.get(m2);
                        GamePlayer man3 = men.get(m3);

                        consumer.accept(new MatchCandidate(
                                MatchType.RANDOM_M3F1,
                                List.of(man1, man2),
                                List.of(man3, woman)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.RANDOM_M3F1,
                                List.of(man1, woman),
                                List.of(man2, man3)
                        ));
                    }
                }
            }
        }
    }

    private void generateRandomM1F3(List<GamePlayer> men, List<GamePlayer> women, Consumer<MatchCandidate> consumer) {
        for (GamePlayer man : men) {
            for (int w1 = 0; w1 < women.size() - 2; w1++) {
                for (int w2 = w1 + 1; w2 < women.size() - 1; w2++) {
                    for (int w3 = w2 + 1; w3 < women.size(); w3++) {
                        GamePlayer woman1 = women.get(w1);
                        GamePlayer woman2 = women.get(w2);
                        GamePlayer woman3 = women.get(w3);

                        consumer.accept(new MatchCandidate(
                                MatchType.RANDOM_M1F3,
                                List.of(man, woman1),
                                List.of(woman2, woman3)
                        ));

                        consumer.accept(new MatchCandidate(
                                MatchType.RANDOM_M1F3,
                                List.of(woman1, woman2),
                                List.of(man, woman3)
                        ));
                    }
                }
            }
        }
    }
}
