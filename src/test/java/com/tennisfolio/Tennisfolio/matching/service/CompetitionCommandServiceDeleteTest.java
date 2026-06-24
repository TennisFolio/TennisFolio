package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.deletedOwnedCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.ownedCompetition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionCommandServiceDeleteTest {

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

    @InjectMocks
    CompetitionCommandService service;

    @Test
    void deleteOwnedCompetition_setsDeletedAt() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 10L))
                .thenReturn(Optional.of(competition));

        service.deleteOwnedCompetition("public-id", 10L);

        assertThat(competition.getDeletedAt()).isNotNull();
        assertThat(competition.getName()).isEqualTo("club");
    }

    @Test
    void deleteOwnedCompetition_isIdempotentForAlreadyDeletedCompetition() {
        Competition competition = deletedOwnedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        java.time.LocalDateTime originalDeletedAt = competition.getDeletedAt();
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 10L))
                .thenReturn(Optional.of(competition));

        service.deleteOwnedCompetition("public-id", 10L);

        assertThat(competition.getDeletedAt()).isEqualTo(originalDeletedAt);
    }

    @Test
    void deleteOwnedCompetition_throwsNotFoundForOtherOwner() {
        when(competitionRepository.findByPublicIdAndOwnerUserId("public-id", 20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOwnedCompetition("public-id", 20L))
                .isInstanceOf(NotFoundException.class);
    }
}
