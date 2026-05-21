package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryCreateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionEntryUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.createEntryRequest;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.entry;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.fixedScheduleCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.updateEntryRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionEntryCommandServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionEntryRepository competitionEntryRepository;

    private CompetitionEntryCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionEntryCommandService(
                competitionRepository,
                competitionEntryRepository
        );
    }

    @Test
    void createCompetitionEntry_addsActiveClubSessionEntryAndIncrementsGenderCount() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        CompetitionEntryCreateRequest request = createEntryRequest(" Newbie ", "male");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.save(any(CompetitionEntry.class))).thenAnswer(invocation -> {
            CompetitionEntry entry = invocation.getArgument(0);
            return entry(10L, entry.getCompetition(), entry.getPlayerName(), entry.getGender());
        });

        CompetitionEntryResponse response = service.createCompetitionEntry("public-id", "edit-token", request);

        assertEquals(4, competition.getMaleCount());
        assertEquals(3, competition.getFemaleCount());
        assertEquals(10L, response.getCompetitionEntryId());
        assertEquals("Newbie", response.getPlayerName());
        assertEquals("MALE", response.getGender());
        assertEquals("ACTIVE", response.getStatus());
        ArgumentCaptor<CompetitionEntry> entryCaptor = ArgumentCaptor.forClass(CompetitionEntry.class);
        verify(competitionEntryRepository).save(entryCaptor.capture());
        assertEquals("Newbie", entryCaptor.getValue().getPlayerName());
        assertEquals(CompetitionEntry.Gender.MALE, entryCaptor.getValue().getGender());
    }

    @Test
    void createCompetitionEntry_rejectsFixedScheduleCompetition() {
        Competition competition = fixedScheduleCompetition(1L, "public-id", "edit-token");
        CompetitionEntryCreateRequest request = createEntryRequest("Player", "MALE");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));

        assertThrows(
                InvalidRequestException.class,
                () -> service.createCompetitionEntry("public-id", "edit-token", request)
        );
        verify(competitionEntryRepository, never()).save(any());
    }

    @Test
    void updateCompetitionEntry_allowsPartialNameGenderAndStatusChanges() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        CompetitionEntry entry = entry(10L, competition, "Old", CompetitionEntry.Gender.MALE);
        CompetitionEntryUpdateRequest request = updateEntryRequest(" New ", "female", "inactive");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(entry));
        CompetitionEntryResponse response = service.updateCompetitionEntry("public-id", 10L, "edit-token", request);

        assertEquals("New", response.getPlayerName());
        assertEquals("FEMALE", response.getGender());
        assertEquals("INACTIVE", response.getStatus());
        assertEquals(CompetitionEntry.Gender.FEMALE, entry.getGender());
        assertEquals(CompetitionEntry.EntryStatus.INACTIVE, entry.getStatus());
    }

    @Test
    void updateCompetitionEntry_allowsInactivatingEntryInReadyGame() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        CompetitionEntry entry = entry(10L, competition, "Player", CompetitionEntry.Gender.MALE);
        CompetitionEntryUpdateRequest request = updateEntryRequest(null, null, "INACTIVE");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(entry));

        CompetitionEntryResponse response = service.updateCompetitionEntry("public-id", 10L, "edit-token", request);

        assertEquals("INACTIVE", response.getStatus());
        assertEquals(CompetitionEntry.EntryStatus.INACTIVE, entry.getStatus());
    }

}
