package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class CandidateGenerator {

    public List<MatchCandidate> generate(List<GamePlayer> players, boolean allowRandom) {
        List<MatchCandidate> result = new ArrayList<>();
        forEachCandidate(players, allowRandom, result::add);
        return result;
    }

    public void forEachCandidate(List<GamePlayer> players, boolean allowRandom, Consumer<MatchCandidate> consumer) {
        List<GamePlayer> men = new ArrayList<>();
        List<GamePlayer> women = new ArrayList<>();

        for (GamePlayer player : players) {
            if (player.gender == GamePlayer.Gender.MALE) {
                men.add(player);
            } else {
                women.add(player);
            }
        }

        generateMixed(men, women, consumer);
        generateMale(men, consumer);
        generateFemale(women, consumer);

        if (allowRandom) {
            generateRandomM3F1(men, women, consumer);
            generateRandomM1F3(men, women, consumer);
        }
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
