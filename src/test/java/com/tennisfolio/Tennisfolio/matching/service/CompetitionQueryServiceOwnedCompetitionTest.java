package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.config.QuerydslConfig;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionDetailResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionSummaryResponse;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({CompetitionQueryService.class, QuerydslConfig.class})
class CompetitionQueryServiceOwnedCompetitionTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionQueryService competitionQueryService;

    @MockitoBean
    private CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    @Test
    void getOwnedCompetitions_returnsOnlyCurrentUsersCompetitionsNewestFirst() {
        Competition firstOwned = competitionRepository.save(new Competition(
                "first",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        competitionRepository.save(new Competition(
                "anonymous",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                null
        ));
        competitionRepository.save(new Competition(
                "other",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                20L
        ));
        Competition secondOwned = competitionRepository.save(new Competition(
                "second",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));

        List<CompetitionSummaryResponse> response =
                competitionQueryService.getOwnedCompetitions(10L);

        assertThat(response)
                .extracting(CompetitionSummaryResponse::getPublicId)
                .containsExactly(secondOwned.getPublicId(), firstOwned.getPublicId());
    }

    @Test
    void getOwnedCompetitions_excludesDeletedCompetitions() {
        Competition active = competitionRepository.save(new Competition(
                "active",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        Competition softDeletedCompetition = competitionRepository.save(new Competition(
                "soft-deleted",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

        List<CompetitionSummaryResponse> response =
                competitionQueryService.getOwnedCompetitions(10L);

        assertThat(response)
                .extracting(CompetitionSummaryResponse::getPublicId)
                .containsExactly(active.getPublicId());
    }

    @Test
    void getCompetition_throwsNotFoundForDeletedCompetition() {
        Competition softDeletedCompetition = competitionRepository.save(new Competition(
                "soft-deleted",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

        assertThatThrownBy(() -> competitionQueryService.getCompetition(
                softDeletedCompetition.getPublicId(),
                10L
        )).isInstanceOf(com.tennisfolio.Tennisfolio.exception.NotFoundException.class);
    }

    @Test
    void getCompetition_marksOwnerPresenceAndCurrentUserOwnership() {
        Competition competition = competitionRepository.save(new Competition(
                "owned",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));

        CompetitionDetailResponse response = competitionQueryService.getCompetition(
                competition.getPublicId(),
                10L
        );

        assertThat(response.getOwnerUserIdSet()).isTrue();
        assertThat(response.getOwnedByCurrentUser()).isTrue();
    }

    @Test
    void getCompetition_marksLinkedClubAdminAsManageableWithoutChangingOwnership() {
        Competition competition = competitionRepository.save(new Competition(
                "club",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                20L
        ));
        when(competitionAdminAuthorizationService.canManageByIdentity(competition, 10L)).thenReturn(true);

        CompetitionDetailResponse response = competitionQueryService.getCompetition(competition.getPublicId(), 10L);

        assertThat(response.getOwnedByCurrentUser()).isFalse();
        assertThat(response.getManageableByCurrentUser()).isTrue();
    }

    @Test
    void getCompetitionResult_throwsNotFoundForDeletedCompetition() {
        Competition softDeletedCompetition = competitionRepository.save(new Competition(
                "soft-deleted",
                4,
                4,
                2,
                1,
                136L,
                Competition.CompetitionMode.CLUB_SESSION,
                10L
        ));
        softDeletedCompetition.delete(java.time.LocalDateTime.of(2026, 6, 23, 12, 0));

        assertThatThrownBy(() -> competitionQueryService.getCompetitionResult(
                softDeletedCompetition.getPublicId()
        )).isInstanceOf(com.tennisfolio.Tennisfolio.exception.NotFoundException.class);
    }
}
