package com.tennisfolio.Tennisfolio.matching.controller;

import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionUpdateResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionAdminAuthorizationService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionEntryQueryService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionGameCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionControllerTest {

    @Mock
    private CompetitionCommandService competitionCommandService;

    @Mock
    private CompetitionQueryService competitionQueryService;

    @Mock
    private CompetitionEntryCommandService competitionEntryCommandService;

    @Mock
    private CompetitionEntryQueryService competitionEntryQueryService;

    @Mock
    private CompetitionGameCommandService competitionGameCommandService;

    @Mock
    private CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    @InjectMocks
    private CompetitionController controller;

    @Test
    void updateCompetition_passesAuthenticatedUserAndToken() {
        Authentication authentication = auth(10L);
        CompetitionUpdateRequest request = new CompetitionUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "Saturday doubles");
        when(competitionCommandService.updateCompetition("public-id", request, 10L, "admin-token"))
                .thenReturn(new CompetitionUpdateResponse(
                        "public-id",
                        "Saturday doubles",
                        LocalDateTime.of(2026, 7, 13, 10, 0)
                ));

        controller.updateCompetition(authentication, "public-id", "admin-token", request);

        verify(competitionCommandService)
                .updateCompetition("public-id", request, 10L, "admin-token");
    }

    @Test
    void createCompetitionEntry_passesAuthenticatedUserAndToken() {
        Authentication authentication = auth(10L);
        CompetitionEntryCreateRequest request = new CompetitionEntryCreateRequest();
        ReflectionTestUtils.setField(request, "playerName", "Alex");
        ReflectionTestUtils.setField(request, "gender", "MALE");

        controller.createCompetitionEntry(authentication, "public-id", "admin-token", request);

        verify(competitionEntryCommandService)
                .createCompetitionEntry("public-id", 10L, "admin-token", request);
    }

    @Test
    void updateGameScore_passesAuthenticatedUserAndToken() {
        Authentication authentication = auth(10L);
        GameScoreUpdateRequest request = new GameScoreUpdateRequest();
        request.setTeamAScore(6);
        request.setTeamBScore(4);
        request.setTeamATiebreakScore(0);
        request.setTeamBTiebreakScore(0);

        controller.updateGameScore(authentication, "public-id", 1L, "admin-token", request);

        verify(competitionGameCommandService)
                .updateGameScore("public-id", 1L, 10L, "admin-token", request);
    }

    private static Authentication auth(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }
}
