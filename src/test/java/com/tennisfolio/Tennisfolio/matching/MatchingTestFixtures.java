package com.tennisfolio.Tennisfolio.matching;

import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CourtCountUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test-only fixture boundary for JPA-managed fields.
 *
 * Keep ReflectionTestUtils usage here instead of scattering it through tests.
 * If a value is part of the public domain API, prefer a real constructor or factory.
 */
public final class MatchingTestFixtures {

    private MatchingTestFixtures() {
    }

    public static Competition clubSessionCompetition(Long id, String publicId, String adminPasswordHash) {
        return competition(id, publicId, adminPasswordHash, Competition.CompetitionMode.CLUB_SESSION);
    }

    public static Competition fixedScheduleCompetition(Long id, String publicId, String adminPasswordHash) {
        return competition(id, publicId, adminPasswordHash, Competition.CompetitionMode.FIXED_SCHEDULE);
    }

    public static Competition competition(
            Long id,
            String publicId,
            String adminPasswordHash,
            Competition.CompetitionMode mode
    ) {
        Competition competition = new Competition("club", 3, 3, 2, 1, 136L, mode);
        ReflectionTestUtils.setField(competition, "id", id);
        ReflectionTestUtils.setField(competition, "publicId", publicId);
        ReflectionTestUtils.setField(competition, "adminPasswordHash", adminPasswordHash);
        return competition;
    }

    public static Competition ownedCompetition(
            Long id,
            String publicId,
            Long ownerUserId,
            Competition.CompetitionMode mode
    ) {
        Competition competition = competition(id, publicId, null, mode);
        ReflectionTestUtils.setField(competition, "ownerUserId", ownerUserId);
        return competition;
    }

    public static Competition deletedOwnedCompetition(
            Long id,
            String publicId,
            Long ownerUserId,
            Competition.CompetitionMode mode
    ) {
        Competition competition = ownedCompetition(id, publicId, ownerUserId, mode);
        competition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));
        return competition;
    }

    public static CompetitionEntry entry(
            Long id,
            Competition competition,
            String playerName,
            CompetitionEntry.Gender gender
    ) {
        CompetitionEntry entry = new CompetitionEntry(competition, playerName, gender);
        ReflectionTestUtils.setField(entry, "id", id);
        return entry;
    }

    public static Game game(Long id, Competition competition, Integer round, Integer court, Game.MatchType matchType) {
        Game game = new Game(competition, round, court, matchType);
        ReflectionTestUtils.setField(game, "id", id);
        return game;
    }

    public static CompetitionEntryCreateRequest createEntryRequest(String playerName, String gender) {
        return request(CompetitionEntryCreateRequest.class, "playerName", playerName, "gender", gender);
    }

    public static CompetitionEntryUpdateRequest updateEntryRequest(
            String playerName,
            String gender,
            String status
    ) {
        CompetitionEntryUpdateRequest request = new CompetitionEntryUpdateRequest();
        if (playerName != null) {
            ReflectionTestUtils.setField(request, "playerName", playerName);
        }
        if (gender != null) {
            ReflectionTestUtils.setField(request, "gender", gender);
        }
        if (status != null) {
            ReflectionTestUtils.setField(request, "status", status);
        }
        return request;
    }

    public static GameStatusUpdateRequest gameStatusUpdateRequest(String status) {
        return request(GameStatusUpdateRequest.class, "status", status);
    }

    public static CourtCountUpdateRequest courtCountUpdateRequest(Integer courtCount) {
        return request(CourtCountUpdateRequest.class, "courtCount", courtCount);
    }

    private static <T> T request(Class<T> type, Object... fieldPairs) {
        try {
            T request = type.getDeclaredConstructor().newInstance();
            for (int i = 0; i < fieldPairs.length; i += 2) {
                ReflectionTestUtils.setField(request, (String) fieldPairs[i], fieldPairs[i + 1]);
            }
            return request;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
