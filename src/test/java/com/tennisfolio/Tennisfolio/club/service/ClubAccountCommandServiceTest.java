package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountRoleUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccountCommandServiceTest {

    @Mock ClubAccountRepository clubAccountRepository;
    @Mock ClubAccessService clubAccessService;

    ClubAccountCommandService service;

    @BeforeEach
    void setUp() {
        service = new ClubAccountCommandService(clubAccountRepository, clubAccessService);
    }

    @Test
    void changeRole_allowsOwnerToPromoteAdmin() {
        Club club = club();
        ClubAccount admin = account(club, 2L, 20L, ClubAccountRole.ADMIN);
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(clubAccountRepository.findByIdAndClub(2L, club)).thenReturn(Optional.of(admin));

        service.changeRole("club", 2L, new ClubAccountRoleUpdateRequest("OWNER"), 10L);

        assertThat(admin.getRole()).isEqualTo(ClubAccountRole.OWNER);
    }

    @Test
    void changeRole_rejectsDemotingLastOwner() {
        Club club = club();
        ClubAccount owner = account(club, 1L, 10L, ClubAccountRole.OWNER);
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(clubAccountRepository.findByIdAndClub(1L, club)).thenReturn(Optional.of(owner));
        when(clubAccountRepository.countByClubAndRole(club, ClubAccountRole.OWNER)).thenReturn(1L);

        assertThatThrownBy(() -> service.changeRole("club", 1L, new ClubAccountRoleUpdateRequest("ADMIN"), 10L))
                .isInstanceOfSatisfying(ResponseStatusException.class, error ->
                        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
                );
    }

    @Test
    void removeAccount_allowsOwnerToRemoveAdmin() {
        Club club = club();
        ClubAccount admin = account(club, 2L, 20L, ClubAccountRole.ADMIN);
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(clubAccountRepository.findByIdAndClub(2L, club)).thenReturn(Optional.of(admin));

        service.removeAccount("club", 2L, 10L);

        verify(clubAccountRepository).delete(admin);
    }

    @Test
    void leaveMyAccount_rejectsLastOwner() {
        Club club = club();
        ClubAccount owner = account(club, 1L, 10L, ClubAccountRole.OWNER);
        when(clubAccessService.requireClubAccount("club", 10L)).thenReturn(owner);
        when(clubAccountRepository.countByClubAndRole(club, ClubAccountRole.OWNER)).thenReturn(1L);

        assertThatThrownBy(() -> service.leaveMyAccount("club", 10L))
                .isInstanceOfSatisfying(ResponseStatusException.class, error ->
                        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
                );
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        return club;
    }

    private static ClubAccount account(Club club, Long id, Long accountId, ClubAccountRole role) {
        ClubAccount account = new ClubAccount(club, accountId, role);
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}
