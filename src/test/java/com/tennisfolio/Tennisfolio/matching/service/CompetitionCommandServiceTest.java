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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
        CompetitionCreateRequest request = new CompetitionCreateRequest();
        request.setMode("CLUB_SESSION");
        request.setCompetitionName("Club");
        request.setMaleCount(4);
        request.setFemaleCount(4);
        request.setCourtCount(2);
        request.setHours(1);
        request.setSeed(136L);
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        ScheduleResult scheduleResult = new ScheduleResult();
        Map<String, CompetitionEntry> entriesByPlayerName = Map.of();

        when(competitionService.createCompetition(request, 1, 136L)).thenReturn(competition);
        when(scheduler.generateSchedule(4, 4, 2, 1, 136L)).thenReturn(scheduleResult);
        when(competitionEntryCommandService.createCompetitionEntries(competition)).thenReturn(entriesByPlayerName);
        when(competitionAdminTokenService.createToken("public-id")).thenReturn("creator-token");

        CompetitionCreateResponse response = service.createCompetition(request);

        assertEquals("public-id", response.getPublicId());
        assertEquals("creator-token", response.getCompetitionAdminToken());
        verify(gameService).saveSchedule(competition, scheduleResult, entriesByPlayerName);
        verify(competitionStatService).createCompetitionStat(competition, scheduleResult, entriesByPlayerName);
    }
}
