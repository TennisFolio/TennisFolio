package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Map;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceTest {

    @Mock
    private TennisMatchScheduler scheduler;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private CompetitionEntryCommandService competitionEntryCommandService;

    @Mock
    private GameService gameService;

    @Mock
    private CompetitionStatService competitionStatService;

    @Mock
    private CompetitionAdminTokenService competitionAdminTokenService;

    @Mock
    private CompetitionRepository competitionRepository;

    private CompetitionCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionCommandService(
                scheduler,
                competitionService,
                competitionEntryCommandService,
                gameService,
                competitionStatService,
                competitionAdminTokenService,
                competitionRepository
        );
    }

    @Test
    void createCompetition_returnsCompetitionAdminToken() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "CLUB_SESSION",
                "Club",
                4,
                4,
                2,
                1,
                136L,
                null,
                null
        );
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 1, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

        CompetitionCreateResponse response = service.createCompetition(request);

        assertEquals("public-id", response.getPublicId());
        assertEquals("creator-token", response.getCompetitionAdminToken());
        verify(competitionEntryCommandService).createCompetitionEntries(competition, request);
        verify(gameService).saveSchedule(competition, scheduleResult, entriesByPlayerName);
        verify(competitionStatService).createCompetitionStat(competition, scheduleResult, entriesByPlayerName);
    }

    @Test
    void createCompetition_passesOwnerUserIdWhenAuthenticated() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "CLUB_SESSION",
                "Club",
                4,
                4,
                2,
                1,
                136L,
                null,
                null
        );
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 1, 136L, 10L)).thenReturn(competition);
        when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

        CompetitionCreateResponse response = service.createCompetition(request, 10L);

        assertEquals("public-id", response.getPublicId());
        verify(competitionService).createCompetition(request, 1, 136L, 10L);
    }

    @Test
    void createCompetition_derivesRoundsFromTotalGamesForFixedSchedule() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                6,
                6,
                3,
                10,
                136L,
                null,
                null
        );
        Competition competition = new Competition("Fixed", 6, 6, 3, 4, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 4, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(6, 6, 3, 10, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

        service.createCompetition(request);

        verify(competitionService).createCompetition(request, 4, 136L);
        verify(scheduler).generateSchedule(6, 6, 3, 10, 136L);
    }

    @Test
    void createCompetition_passesSameGenderOnlyPolicyForFixedSchedule() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                8,
                2,
                8,
                136L,
                null,
                null,
                true
        );
        Competition competition = new Competition("Fixed", 8, 8, 2, 4, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 4, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(
                eq(8),
                eq(8),
                eq(2),
                eq(8),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        )).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(
                8,
                8,
                2,
                8,
                136L,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );
        verify(scheduler, never()).generateSchedule(8, 8, 2, 8, 136L);
    }

    @Test
    void createCompetition_ignoresSameGenderOnlyForClubSession() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "CLUB_SESSION",
                "Club",
                4,
                4,
                2,
                1,
                136L,
                null,
                null,
                true
        );
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 1, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(4, 4, 2, 1, 136L);
        verify(scheduler, never()).generateSchedule(
                eq(4),
                eq(4),
                eq(2),
                eq(1),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        );
    }

    @Test
    void createCompetition_rejectsSameGenderOnlyWhenIncludedMaleCountIsLessThanFour() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                3,
                8,
                1,
                1,
                136L,
                null,
                null,
                true
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createCompetition(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "sameGenderDoublesOnly requires each included gender to have at least 4 players",
                exception.getReason()
        );
    }

    @Test
    void createCompetition_rejectsSameGenderOnlyWhenIncludedFemaleCountIsLessThanFour() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                3,
                1,
                1,
                136L,
                null,
                null,
                true
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createCompetition(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "sameGenderDoublesOnly requires each included gender to have at least 4 players",
                exception.getReason()
        );
    }

    @Test
    void createCompetition_allowsSameGenderOnlyWhenFemaleCountIsZero() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                8,
                0,
                2,
                4,
                136L,
                null,
                null,
                true
        );
        Competition competition = new Competition("Fixed", 8, 0, 2, 2, 136L, Competition.CompetitionMode.FIXED_SCHEDULE);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 2, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(
                eq(8),
                eq(0),
                eq(2),
                eq(4),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        )).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition, request)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken(competition.getPublicId())).thenReturn("creator-token");

        service.createCompetition(request);

        verify(scheduler).generateSchedule(
                8,
                0,
                2,
                4,
                136L,
                EnumSet.of(MatchType.MALE, MatchType.FEMALE)
        );
    }

    @Test
    void createCompetition_rejectsSameGenderOnlyWhenGameCountsCannotBeAllocated() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                5,
                5,
                1,
                5,
                136L,
                null,
                null,
                true
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createCompetition(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "sameGenderDoublesOnly cannot allocate same-gender game counts for the requested player distribution",
                exception.getReason()
        );
        verify(scheduler, never()).generateSchedule(
                eq(5),
                eq(5),
                eq(1),
                eq(5),
                eq(136L),
                eq(EnumSet.of(MatchType.MALE, MatchType.FEMALE))
        );
    }

    @Test
    void createCompetition_rejectsNonPositiveTotalGamesForFixedSchedule() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                4,
                4,
                2,
                0,
                136L,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> service.createCompetition(request));
    }

    @Test
    void createCompetition_rejectsTotalGamesGreaterThanCourtCountTimesTwenty() {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "FIXED_SCHEDULE",
                "Fixed",
                4,
                4,
                2,
                41,
                136L,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> service.createCompetition(request));
    }
}
