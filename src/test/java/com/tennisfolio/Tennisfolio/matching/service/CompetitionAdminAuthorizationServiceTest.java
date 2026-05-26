package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.tennisfolio.Tennisfolio.matching.MatchingTestFixtures.clubSessionCompetition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionAdminAuthorizationServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private CompetitionAdminTokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CompetitionAdminAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionAdminAuthorizationService(
                competitionRepository,
                tokenService,
                passwordEncoder
        );
    }

    @Test
    void setAdminPassword_storesHashAndReturnsToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        assertFalse(competition.hasAdminPassword());
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.validateAndGetPublicId("creator-token")).thenReturn("public-id");
        when(tokenService.createToken("public-id")).thenReturn("fresh-token");

        String token = service.setAdminPassword("public-id", "creator-token", "1234");

        assertEquals("fresh-token", token);
        assertTrue(competition.hasAdminPassword());
        assertNotEquals("1234", competition.getAdminPasswordHash());
        assertTrue(passwordEncoder.matches("1234", competition.getAdminPasswordHash()));
    }

    @Test
    void setAdminPassword_rejectsViewerWithoutAdminToken() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.validateAndGetPublicId(null))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid competition admin token"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", null, "1234")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertFalse(competition.hasAdminPassword());
    }

    @Test
    void setAdminPassword_rejectsInvalidPasswordFormat() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.validateAndGetPublicId("creator-token")).thenReturn("public-id");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", "creator-token", "12ab")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void setAdminPassword_rejectsAlreadySetPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.validateAndGetPublicId("creator-token")).thenReturn("public-id");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.setAdminPassword("public-id", "creator-token", "5678")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void login_returnsTokenWhenPasswordMatches() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));
        when(tokenService.createToken("public-id")).thenReturn("admin-token");

        assertEquals("admin-token", service.login("public-id", "1234"));
    }

    @Test
    void login_rejectsUnsetAdminPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", null);
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("public-id", "1234")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void login_rejectsWrongPassword() {
        Competition competition = clubSessionCompetition(1L, "public-id", passwordEncoder.encode("1234"));
        when(competitionRepository.findByPublicId("public-id")).thenReturn(Optional.of(competition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("public-id", "5678")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void validateAdminToken_rejectsOtherCompetitionToken() {
        when(tokenService.validateAndGetPublicId("admin-token")).thenReturn("other-public-id");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateAdminToken("public-id", "admin-token")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}
