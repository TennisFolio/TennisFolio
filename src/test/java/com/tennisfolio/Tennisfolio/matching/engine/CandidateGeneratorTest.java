package com.tennisfolio.Tennisfolio.matching.engine;

import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchCandidate;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateGeneratorTest {

    private final CandidateGenerator generator = new CandidateGenerator();

    @Test
    void generateWithAllowedTypesCreatesOnlyMaleAndFemaleCandidates() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"), male("M3"), male("M4"),
                female("F1"), female("F2"), female("F3"), female("F4")
        );

        List<MatchCandidate> candidates = generator.generate(
                players,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        Set<MatchType> types = candidates.stream()
                .map(candidate -> candidate.type)
                .collect(Collectors.toSet());

        assertEquals(Set.of(MatchType.MALE, MatchType.FEMALE), types);
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.MIXED));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.RANDOM_M3F1));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.type == MatchType.RANDOM_M1F3));
    }

    @Test
    void generateWithAllowedTypesDoesNotFallbackToMixedOrRandom() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"),
                female("F1"), female("F2")
        );

        List<MatchCandidate> candidates = generator.generate(
                players,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );

        assertTrue(candidates.isEmpty());
    }

    @Test
    void existingAllowRandomFalsePathKeepsNormalTypes() {
        List<GamePlayer> players = List.of(
                male("M1"), male("M2"), male("M3"), male("M4"),
                female("F1"), female("F2"), female("F3"), female("F4")
        );

        List<MatchCandidate> candidates = generator.generate(players, false);

        Set<MatchType> types = candidates.stream()
                .map(candidate -> candidate.type)
                .collect(Collectors.toSet());

        assertEquals(Set.of(MatchType.MIXED, MatchType.MALE, MatchType.FEMALE), types);
    }

    private GamePlayer male(String id) {
        return new GamePlayer(id, GamePlayer.Gender.MALE);
    }

    private GamePlayer female(String id) {
        return new GamePlayer(id, GamePlayer.Gender.FEMALE);
    }
}
