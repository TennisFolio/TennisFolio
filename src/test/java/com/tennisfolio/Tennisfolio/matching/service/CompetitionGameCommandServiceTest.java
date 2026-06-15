package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.exception.InvalidRequestException;
import com.tennisfolio.Tennisfolio.matching.domain.GameMatch;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.MatchType;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionStatResponse;
import com.tennisfolio.Tennisfolio.matching.dto.CourtCountUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameResponse;
import com.tennisfolio.Tennisfolio.matching.dto.GameScoreUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.dto.GameStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionEntry;
import com.tennisfolio.Tennisfolio.matching.entity.CompetitionStat;
import com.tennisfolio.Tennisfolio.matching.entity.Game;
import com.tennisfolio.Tennisfolio.matching.entity.GameEntry;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionStatRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameEntryRepository;
import com.tennisfolio.Tennisfolio.matching.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.courtCountUpdateRequest;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.entry;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.game;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.gameStatusUpdateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionGameCommandServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionEntryRepository competitionEntryRepository;

    @Mock
    private CompetitionStatRepository competitionStatRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameEntryRepository gameEntryRepository;

    @Mock
    private TennisMatchScheduler scheduler;

    @Mock
    private GameService gameService;

    @Mock
    private CompetitionAdminAuthorizationService competitionAdminAuthorizationService;

    private CompetitionGameCommandService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionGameCommandService(
                competitionRepository,
                competitionEntryRepository,
                competitionStatRepository,
                gameRepository,
                gameEntryRepository,
                scheduler,
                gameService,
                competitionAdminAuthorizationService
        );
    }

    @Test
    void createNextCourtGame_usesAllActivePlayersAndNextRoundForRequestedCourt() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "M3", CompetitionEntry.Gender.MALE),
                entry(4L, competition, "F1", CompetitionEntry.Gender.FEMALE),
                entry(5L, competition, "F2", CompetitionEntry.Gender.FEMALE),
                entry(6L, competition, "F3", CompetitionEntry.Gender.FEMALE)
        );
        GameMatch match = new GameMatch(
                3,
                1,
                MatchType.MIXED,
                List.of(player(2L, GamePlayer.Gender.MALE), player(4L, GamePlayer.Gender.FEMALE)),
                List.of(player(3L, GamePlayer.Gender.MALE), player(5L, GamePlayer.Gender.FEMALE))
        );
        Game savedGame = game(20L, competition, 3, 1, Game.MatchType.MIXED);
        List<GameEntry> savedGameEntries = List.of(
                new GameEntry(savedGame, entries.get(1), GameEntry.Team.A, 1),
                new GameEntry(savedGame, entries.get(3), GameEntry.Team.A, 2),
                new GameEntry(savedGame, entries.get(2), GameEntry.Team.B, 1),
                new GameEntry(savedGame, entries.get(4), GameEntry.Team.B, 2)
        );

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByCompetitionIdAndStatus(1L, CompetitionEntry.EntryStatus.ACTIVE))
                .thenReturn(entries);
        when(gameRepository.findMaxRoundByCompetitionIdAndCourt(1L, 1)).thenReturn(2);
        when(gameEntryRepository.findScheduleEntriesByCompetitionId(1L)).thenReturn(List.of());
        when(scheduler.generateNextClubSessionGame(any(), eq(List.of()), eq(1), eq(3), anyLong())).thenReturn(match);
        when(gameService.saveGame(eq(competition), eq(match), any())).thenReturn(savedGame);
        when(competitionEntryRepository.findByCompetitionId(1L)).thenReturn(entries);
        when(competitionStatRepository.findByCompetitionId(1L)).thenReturn(Optional.empty());
        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameEntryRepository.findByGameId(20L)).thenReturn(savedGameEntries);

        GameResponse response = service.createNextCourtGame("public-id", 1, "admin-token");

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals(20L, response.getGameId());
        assertEquals(3, response.getRound());
        assertEquals(1, response.getCourt());
        ArgumentCaptor<List<CompetitionEntry>> candidateCaptor = ArgumentCaptor.forClass(List.class);
        verify(scheduler).generateNextClubSessionGame(
                candidateCaptor.capture(),
                eq(List.of()),
                eq(1),
                eq(3),
                anyLong()
        );
        Set<Long> candidateIds = candidateCaptor.getValue().stream()
                .map(CompetitionEntry::getId)
                .collect(Collectors.toSet());
        assertEquals(entries.stream().map(CompetitionEntry::getId).collect(Collectors.toSet()), candidateIds);
    }

    @Test
    void createNextCourtGame_allowsRequestedCourtAlreadyHasReadyGame() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        List<CompetitionEntry> entries = List.of(
                entry(1L, competition, "M1", CompetitionEntry.Gender.MALE),
                entry(2L, competition, "M2", CompetitionEntry.Gender.MALE),
                entry(3L, competition, "F1", CompetitionEntry.Gender.FEMALE),
                entry(4L, competition, "F2", CompetitionEntry.Gender.FEMALE)
        );
        GameMatch match = new GameMatch(
                2,
                1,
                MatchType.MIXED,
                List.of(player(1L, GamePlayer.Gender.MALE), player(3L, GamePlayer.Gender.FEMALE)),
                List.of(player(2L, GamePlayer.Gender.MALE), player(4L, GamePlayer.Gender.FEMALE))
        );
        Game savedGame = game(20L, competition, 2, 1, Game.MatchType.MIXED);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByCompetitionIdAndStatus(1L, CompetitionEntry.EntryStatus.ACTIVE))
                .thenReturn(entries);
        when(gameRepository.findMaxRoundByCompetitionIdAndCourt(1L, 1)).thenReturn(1);
        when(gameEntryRepository.findScheduleEntriesByCompetitionId(1L)).thenReturn(List.of());
        when(scheduler.generateNextClubSessionGame(any(), eq(List.of()), eq(1), eq(2), anyLong())).thenReturn(match);
        when(gameService.saveGame(eq(competition), eq(match), any())).thenReturn(savedGame);
        when(competitionEntryRepository.findByCompetitionId(1L)).thenReturn(entries);
        when(competitionStatRepository.findByCompetitionId(1L)).thenReturn(Optional.empty());
        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameEntryRepository.findByGameId(20L)).thenReturn(List.of());

        GameResponse response = service.createNextCourtGame("public-id", 1, "admin-token");

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals(20L, response.getGameId());
        assertEquals(2, response.getRound());
        verify(scheduler).generateNextClubSessionGame(any(), any(), eq(1), eq(2), anyLong());
    }

    @Test
    void updateGameStatus_completesReadyClubSessionGame() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        Game game = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        GameStatusUpdateRequest request = gameStatusUpdateRequest("completed");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(game));
        when(gameEntryRepository.findByGameId(10L)).thenReturn(List.of());

        GameResponse response = service.updateGameStatus("public-id", 10L, "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(Game.GameStatus.COMPLETED, game.getStatus());
    }

    @Test
    void updateGameStatus_rejectsInvalidAdminToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        Game game = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        GameStatusUpdateRequest request = gameStatusUpdateRequest("completed");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid competition admin token"))
                .when(competitionAdminAuthorizationService)
                .validateAdminToken("public-id", "bad-token");

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateGameStatus("public-id", 10L, "bad-token", request)
        );
        verify(gameRepository, never()).findByIdAndCompetitionId(10L, 1L);
    }

    @Test
    void updateGameStatus_rejectsCompletedGame() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        Game game = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        game.complete();
        GameStatusUpdateRequest request = gameStatusUpdateRequest("completed");

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(game));

        assertThrows(
                InvalidRequestException.class,
                () -> service.updateGameStatus("public-id", 10L, "admin-token", request)
        );
    }

    @Test
    void deleteGame_deletesReadyGameEntriesAndRecalculatesStats() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        Game game = game(10L, competition, 1, 1, Game.MatchType.MIXED);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(game));
        when(competitionEntryRepository.findByCompetitionId(1L)).thenReturn(List.of());
        when(gameEntryRepository.findScheduleEntriesByCompetitionId(1L)).thenReturn(List.of());
        when(competitionStatRepository.findByCompetitionId(1L)).thenReturn(Optional.empty());
        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteGame("public-id", 10L, "admin-token");

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        verify(gameEntryRepository).deleteByGameId(10L);
        verify(gameRepository).delete(game);
        verify(competitionStatRepository).save(any(CompetitionStat.class));
    }

    @Test
    void updateGameScore_allowsCompletedClubSessionGameWithoutAdminToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        Game game = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        game.complete();
        GameScoreUpdateRequest request = new GameScoreUpdateRequest();
        request.setTeamAScore(6);
        request.setTeamBScore(4);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByIdAndCompetitionId(10L, 1L)).thenReturn(Optional.of(game));
        when(gameEntryRepository.findByGameId(10L)).thenReturn(List.of());

        GameResponse response = service.updateGameScore("public-id", 10L, null, request);

        verify(competitionAdminAuthorizationService, never()).validateAdminToken(any(), any());
        assertEquals(6, response.getScore().getTeamAScore());
        assertEquals(4, response.getScore().getTeamBScore());
    }

    @Test
    void updateCourtCount_allowsClubSessionCountBetweenOneAndTen() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        CourtCountUpdateRequest request = courtCountUpdateRequest(3);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(competitionEntryRepository.findByCompetitionId(1L)).thenReturn(List.of());
        when(gameEntryRepository.findScheduleEntriesByCompetitionId(1L)).thenReturn(List.of());
        when(competitionStatRepository.findByCompetitionId(1L)).thenReturn(Optional.empty());
        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompetitionStatResponse response = service.updateCourtCount("public-id", "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals(3, competition.getCourtCount());
        assertEquals(0, response.getTotalGames());
    }

    @Test
    void updateCourtCount_relocatesReadyGameOnRemovedCourtToEmptyRemainingCourt() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        competition.updateCourtCount(3);
        Game readyOnCourt2 = game(10L, competition, 1, 2, Game.MatchType.MIXED);
        Game readyOnCourt3 = game(11L, competition, 1, 3, Game.MatchType.MIXED);
        CourtCountUpdateRequest request = courtCountUpdateRequest(2);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByCompetitionIdAndStatus(1L, Game.GameStatus.READY))
                .thenReturn(List.of(readyOnCourt2, readyOnCourt3));
        when(competitionEntryRepository.findByCompetitionId(1L)).thenReturn(List.of());
        when(gameEntryRepository.findScheduleEntriesByCompetitionId(1L)).thenReturn(List.of());
        when(competitionStatRepository.findByCompetitionId(1L)).thenReturn(Optional.empty());
        when(competitionStatRepository.save(any(CompetitionStat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateCourtCount("public-id", "admin-token", request);

        verify(competitionAdminAuthorizationService).validateAdminToken("public-id", "admin-token");
        assertEquals(2, competition.getCourtCount());
        assertEquals(1, readyOnCourt3.getCourt());
        assertEquals(2, readyOnCourt2.getCourt());
    }

    @Test
    void updateCourtCount_rejectsWhenRemovedCourtReadyGameHasNoEmptyRemainingCourt() {
        Competition competition = clubSessionCompetition(1L, "public-id", "edit-token");
        competition.updateCourtCount(3);
        Game readyOnCourt1 = game(10L, competition, 1, 1, Game.MatchType.MIXED);
        Game readyOnCourt2 = game(11L, competition, 1, 2, Game.MatchType.MIXED);
        Game readyOnCourt3 = game(12L, competition, 1, 3, Game.MatchType.MIXED);
        CourtCountUpdateRequest request = courtCountUpdateRequest(2);

        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(gameRepository.findByCompetitionIdAndStatus(1L, Game.GameStatus.READY))
                .thenReturn(List.of(readyOnCourt1, readyOnCourt2, readyOnCourt3));

        assertThrows(
                InvalidRequestException.class,
                () -> service.updateCourtCount("public-id", "admin-token", request)
        );
        assertEquals(3, competition.getCourtCount());
        assertEquals(3, readyOnCourt3.getCourt());
        verify(competitionStatRepository, never()).save(any());
    }

    private GamePlayer player(Long entryId, GamePlayer.Gender gender) {
        return new GamePlayer(String.valueOf(entryId), gender);
    }
}
