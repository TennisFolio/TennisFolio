package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.club.service.ClubAccessService;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.ownedCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionAdminAuthorizationServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionAdminTokenService tokenService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ClubAccessService clubAccessService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CompetitionAdminAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionAdminAuthorizationService(
                competitionRepository,
                tokenService,
                passwordEncoder,
                meetingRepository,
                clubAccessService
        );
    }

    @Test
    void setAdminPassword_storesHashAndReturnsToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        assertFalse(competition.hasAdminPassword());
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));
        when(tokenService.validateAndGetPublicId("creator-token")).thenReturn("public-id");
        when(tokenService.createToken("public-id")).thenReturn("fresh-token");

        String token = service.setAdminPassword("public-id", null, "creator-token", "1234");

        assertEquals("fresh-token", token);
        assertTrue(competition.hasAdminPassword());
        assertNotEquals("1234", competition.getAdminPasswordHash());
        assertTrue(passwordEncoder.matches("1234", competition.getAdminPasswordHash()));
    }

    @Test
    void setAdminPassword_rejectsViewerWithoutAdminToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));
        when(meetingRepository.findByCompetitionIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", 10L, null, "1234")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertFalse(competition.hasAdminPassword());
    }

    @Test
    void setAdminPassword_rejectsInvalidPasswordFormat() {
        Competition competition = ownedCompetition(1L, "public-id", 10L, Competition.CompetitionMode.CLUB_SESSION);
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", 10L, null, "12ab")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void setAdminPassword_rejectsAlreadySetPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", null, "creator-token", "5678")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void login_returnsTokenWhenPasswordMatches() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.createToken("public-id")).thenReturn("admin-token");

        assertEquals("admin-token", service.login("public-id", "1234"));
    }

    @Test
    void login_rejectsUnsetAdminPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id")).thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("public-id", "1234")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void login_rejectsWrongPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id")).thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("public-id", "5678")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void login_rejectsDeletedCompetitionAsNotFound() {
        when(competitionRepository.findByPublicIdAndDeletedAtIsNull("public-id"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.login("public-id", "1234")
        );
    }

    @Test
    void validateManagementAccess_rejectsOtherCompetitionToken() {
        Competition competition = ownedCompetition(1L, "public-id", 20L, Competition.CompetitionMode.CLUB_SESSION);
        when(tokenService.validateAndGetPublicId("admin-token")).thenReturn("other-public-id");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateManagementAccess(competition, null, "admin-token")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void validateManagementAccess_allowsCompetitionOwnerWithoutToken() {
        Competition competition = ownedCompetition(
                1L,
                "public-id",
                10L,
                Competition.CompetitionMode.CLUB_SESSION
        );
        service.validateManagementAccess(competition, 10L, null);

        verifyNoInteractions(tokenService, meetingRepository, clubAccessService);
    }

    @Test
    void validateManagementAccess_allowsLinkedClubAdminWithoutToken() {
        Competition competition = ownedCompetition(1L, "public-id", 20L, Competition.CompetitionMode.CLUB_SESSION);
        Meeting meeting = clubMeeting(1L, 100L);
        when(meetingRepository.findByCompetitionIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(meeting));
        when(clubAccessService.isActiveAdmin(100L, 10L)).thenReturn(true);

        service.validateManagementAccess(competition, 10L, null);

        verifyNoInteractions(tokenService);
    }

    @Test
    void validateManagementAccess_allowsValidTokenForNonMember() {
        Competition competition = ownedCompetition(1L, "public-id", 20L, Competition.CompetitionMode.CLUB_SESSION);
        when(tokenService.validateAndGetPublicId("admin-token")).thenReturn("public-id");

        service.validateManagementAccess(competition, null, "admin-token");
    }

    @Test
    void validateManagementAccess_allowsIdentityWhenStaleTokenIsPresent() {
        Competition competition = ownedCompetition(1L, "public-id", 10L, Competition.CompetitionMode.CLUB_SESSION);

        service.validateManagementAccess(competition, 10L, "stale-token");

        verifyNoInteractions(tokenService);
    }

    @Test
    void validateManagementAccess_rejectsMemberWithoutToken() {
        Competition competition = ownedCompetition(1L, "public-id", 20L, Competition.CompetitionMode.CLUB_SESSION);
        Meeting meeting = clubMeeting(1L, 100L);
        when(meetingRepository.findByCompetitionIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(meeting));
        when(clubAccessService.isActiveAdmin(100L, 10L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateManagementAccess(competition, 10L, null)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void setAdminPassword_allowsLinkedClubAdminWithoutEditToken() {
        Competition competition = ownedCompetition(1L, "public-id", 20L, Competition.CompetitionMode.CLUB_SESSION);
        Meeting meeting = clubMeeting(1L, 100L);
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));
        when(meetingRepository.findByCompetitionIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(meeting));
        when(clubAccessService.isActiveAdmin(100L, 10L)).thenReturn(true);
        when(tokenService.createToken("public-id")).thenReturn("fresh-token");

        String token = service.setAdminPassword("public-id", 10L, null, "1234");

        assertEquals("fresh-token", token);
        assertTrue(passwordEncoder.matches("1234", competition.getAdminPasswordHash()));
    }

    @Test
    void setAdminPassword_rejectsSecondSetup() {
        Competition competition = ownedCompetition(1L, "public-id", 10L, Competition.CompetitionMode.CLUB_SESSION);
        competition.setAdminPasswordHash(passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate("public-id"))
                .thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", 10L, null, "5678")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private static Meeting clubMeeting(Long competitionId, Long clubId) {
        Meeting meeting = new Meeting(
                10L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        );
        ReflectionTestUtils.setField(meeting, "id", 50L);
        ReflectionTestUtils.setField(meeting, "publicId", "meeting-public-id");
        meeting.connectClub(clubId);
        meeting.connectCompetition(competitionId);
        return meeting;
    }
}
