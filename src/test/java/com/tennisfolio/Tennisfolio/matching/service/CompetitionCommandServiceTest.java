package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.domain.ScheduleResult;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private CompetitionCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionCommandService(
                scheduler,
                competitionService,
                competitionEntryCommandService,
                gameService,
                competitionStatService,
                competitionAdminTokenService
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
