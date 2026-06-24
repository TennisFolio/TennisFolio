package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.ownedCompetition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceClaimTest {

    @Mock
    TennisMatchScheduler scheduler;

    @Mock
    CompetitionService competitionService;

    @Mock
    CompetitionEntryCommandService competitionEntryCommandService;

    @Mock
    GameService gameService;

    @Mock
    CompetitionStatService competitionStatService;

    @Mock
    CompetitionAdminTokenService competitionAdminTokenService;

    @Mock
    CompetitionRepository competitionRepository;

    CompetitionCommandService service;

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
    void claimCompetition_setsOwnerWhenCompetitionHasNoOwner() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                null,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));
        when(competitionAdminTokenService.validateAndGetPublicId("admin-token"))
                .thenReturn("public-id");

        service.claimCompetition("public-id", 10L, "admin-token");

        verify(competitionAdminTokenService).validateAndGetPublicId("admin-token");
        assertThat(competition.getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void claimCompetition_isIdempotentForCurrentOwner() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));

        service.claimCompetition("public-id", 10L, "admin-token");

        assertThat(competition.getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void claimCompetition_throwsConflictWhenAnotherOwnerExists() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                20L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));
        when(competitionAdminTokenService.validateAndGetPublicId("admin-token"))
                .thenReturn("public-id");

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void claimCompetition_throwsNotFoundWhenCompetitionIsDeleted() {
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void claimCompetition_throwsForbiddenWhenTokenPublicIdDiffers() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                null,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.of(competition));
        when(competitionAdminTokenService.validateAndGetPublicId("admin-token"))
                .thenReturn("other-public-id");

        assertThatThrownBy(() -> service.claimCompetition("public-id", 10L, "admin-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }
}
