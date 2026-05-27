package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.createEntryRequest;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.entry;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.fixedScheduleCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.updateEntryRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionEntryCommandServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionEntryRepository competitionEntryRepository;

    @Mock
    private CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    private CompetitionEntryCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionEntryCommandService(
                competitionRepository,
                competitionEntryRepository,
                competitionAdminAuthorizationService
        );
    }

    @Test
    void createCompetitionEntries_usesProvidedPlayerNames() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionCreateRequest request = createCompetitionRequest(
                List.of("민수", " M2 ", "준호"),
                List.of("지연", "F2", "수진")
        );

        when(competitionEntryRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<CompetitionEntry> entries = invocation.getArgument(0);
            List<CompetitionEntry> savedEntries = new ArrayList<>();
            entries.forEach(savedEntries::add);
            return savedEntries;
        });

        service.createCompetitionEntries(competition, request);

        ArgumentCaptor<Iterable<CompetitionEntry>> entriesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(competitionEntryRepository).saveAll(entriesCaptor.capture());
        List<CompetitionEntry> entries = new ArrayList<>();
        entriesCaptor.getValue().forEach(entries::add);

        assertEquals("민수", entries.get(0).getPlayerName());
        assertEquals("M2", entries.get(1).getPlayerName());
        assertEquals("준호", entries.get(2).getPlayerName());
        assertEquals("지연", entries.get(3).getPlayerName());
        assertEquals("F2", entries.get(4).getPlayerName());
        assertEquals("수진", entries.get(5).getPlayerName());
    }

    @Test
    void createCompetitionEntries_fallsBackToDefaultNamesWhenNamesAreMissingOrBlank() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionCreateRequest request = createCompetitionRequest(
                List.of("민수", " ", ""),
                List.of("지연")
        );

        when(competitionEntryRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<CompetitionEntry> entries = invocation.getArgument(0);
            List<CompetitionEntry> savedEntries = new ArrayList<>();
            entries.forEach(savedEntries::add);
            return savedEntries;
        });

        service.createCompetitionEntries(competition, request);

        ArgumentCaptor<Iterable<CompetitionEntry>> entriesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(competitionEntryRepository).saveAll(entriesCaptor.capture());
        List<CompetitionEntry> entries = new ArrayList<>();
        entriesCaptor.getValue().forEach(entries::add);

        assertEquals("민수", entries.get(0).getPlayerName());
        assertEquals("M2", entries.get(1).getPlayerName());
        assertEquals("M3", entries.get(2).getPlayerName());
        assertEquals("지연", entries.get(3).getPlayerName());
        assertEquals("F2", entries.get(4).getPlayerName());
        assertEquals("F3", entries.get(5).getPlayerName());
    }

    @Test
    void createCompetitionEntries_keepsDefaultNamesWhenRequestHasNoNames() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionCreateRequest request = createCompetitionRequest(null, null);

        when(competitionEntryRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<CompetitionEntry> entries = invocation.getArgument(0);
            List<CompetitionEntry> savedEntries = new ArrayList<>();
            entries.forEach(savedEntries::add);
            return savedEntries;
        });

        service.createCompetitionEntries(competition, request);

        ArgumentCaptor<Iterable<CompetitionEntry>> entriesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(competitionEntryRepository).saveAll(entriesCaptor.capture());
        List<CompetitionEntry> entries = new ArrayList<>();
        entriesCaptor.getValue().forEach(entries::add);

        assertEquals("M1", entries.get(0).getPlayerName());
        assertEquals("M2", entries.get(1).getPlayerName());
        assertEquals("M3", entries.get(2).getPlayerName());
        assertEquals("F1", entries.get(3).getPlayerName());
        assertEquals("F2", entries.get(4).getPlayerName());
        assertEquals("F3", entries.get(5).getPlayerName());
    }

    @Test
    void createCompetitionEntries_allowsDuplicateNames() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionCreateRequest request = createCompetitionRequest(
                List.of("민수", "민수", "민수"),
                null
        );

        when(competitionEntryRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<CompetitionEntry> entries = invocation.getArgument(0);
            List<CompetitionEntry> savedEntries = new ArrayList<>();
            entries.forEach(savedEntries::add);
            return savedEntries;
        });

        service.createCompetitionEntries(competition, request);

        ArgumentCaptor<Iterable<CompetitionEntry>> entriesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(competitionEntryRepository).saveAll(entriesCaptor.capture());
        List<CompetitionEntry> entries = new ArrayList<>();
        entriesCaptor.getValue().forEach(entries::add);

        assertEquals("민수", entries.get(0).getPlayerName());
        assertEquals("민수", entries.get(1).getPlayerName());
        assertEquals("민수", entries.get(2).getPlayerName());
    }

    @Test
    void createCompetitionEntries_rejectsNamesLongerThanNineCharacters() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionCreateRequest request = createCompetitionRequest(
                List.of("1234567890"),
                null
        );

        assertThrows(
                InvalidRequestException.class,
                () -> service.createCompetitionEntries(competition, request)
        );
        verify(competitionEntryRepository, never()).saveAll(any());
    }

    private CompetitionCreateRequest createCompetitionRequest(
            List<String> malePlayerNames,
            List<String> femalePlayerNames
    ) {
        return new CompetitionCreateRequest(
                "CLUB_SESSION",
                "Club",
                3,
                3,
                2,
                1,
                136L,
                malePlayerNames,
                femalePlayerNames
        );
    }

    @Test
    void createCompetitionEntry_addsActiveClubSessionEntryAndIncrementsGenderCount() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionEntryCreateRequest request = createEntryRequest(" Newbie ", "male");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.save(any(CompetitionEntry.class))).thenAnswer(invocation -> {
            CompetitionEntry entry = invocation.getArgument(0);
            return entry(10L, entry.getCompetition(), entry.getPlayerName(), entry.getGender());
        });

        CompetitionEntryResponse response = service.createCompetitionEntry("public-id", "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
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
        Competition competition = fixedScheduleCompetition(1L, "public-id", null);
        CompetitionEntryCreateRequest request = createEntryRequest("Player", "MALE");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));

        assertThrows(
                InvalidRequestException.class,
                () -> service.createCompetitionEntry("public-id", "admin-token", request)
        );
        verify(competitionEntryRepository, never()).save(any());
    }

    @Test
    void createCompetitionEntry_rejectsInvalidAdminToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionEntryCreateRequest request = createEntryRequest("Player", "MALE");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid competition admin token"))
                .when(competitionAdminAuthorizationService)
                .validateAdminToken("public-id", "bad-token");

        assertThrows(
                ResponseStatusException.class,
                () -> service.createCompetitionEntry("public-id", "bad-token", request)
        );
        verify(competitionEntryRepository, never()).save(any());
    }

    @Test
    void updateCompetitionEntry_allowsPartialNameGenderAndStatusChanges() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionEntry entry = entry(10L, competition, "Old", CompetitionEntry.Gender.MALE);
        CompetitionEntryUpdateRequest request = updateEntryRequest(" New ", "female", "inactive");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(entry));
        CompetitionEntryResponse response = service.updateCompetitionEntry("public-id", 10L, "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals("New", response.getPlayerName());
        assertEquals("FEMALE", response.getGender());
        assertEquals("INACTIVE", response.getStatus());
        assertEquals(CompetitionEntry.Gender.FEMALE, entry.getGender());
        assertEquals(CompetitionEntry.EntryStatus.INACTIVE, entry.getStatus());
    }

    @Test
    void updateCompetitionEntry_allowsInactivatingEntryInReadyGame() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        CompetitionEntry entry = entry(10L, competition, "Player", CompetitionEntry.Gender.MALE);
        CompetitionEntryUpdateRequest request = updateEntryRequest(null, null, "INACTIVE");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(entry));

        CompetitionEntryResponse response = service.updateCompetitionEntry("public-id", 10L, "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals("INACTIVE", response.getStatus());
        assertEquals(CompetitionEntry.EntryStatus.INACTIVE, entry.getStatus());
    }

}
