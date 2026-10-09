package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccountQueryServiceTest {

    @Mock ClubAccountRepository clubAccountRepository;
    @Mock ClubAccessService clubAccessService;

    ClubAccountQueryService service;

    @BeforeEach
    void setUp() {
        service = new ClubAccountQueryService(clubAccountRepository, clubAccessService);
    }

    @Test
    void getAccounts_returnsClubAccountsForOwner() {
        Club club = club();
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(clubAccountRepository.findByClubOrderByIdAsc(club)).thenReturn(List.of(
                account(club, 1L, 10L, "Alex", ClubAccountRole.OWNER),
                account(club, 2L, 20L, "Jamie", ClubAccountRole.ADMIN)
        ));

        List<ClubAccountResponse> result = service.getAccounts("club", 10L);

        assertThat(result).extracting(ClubAccountResponse::getDisplayName)
                .containsExactly("Alex", "Jamie");
        assertThat(result).extracting(ClubAccountResponse::getRole)
                .containsExactly("OWNER", "ADMIN");
    }

    @Test
    void getMyAccount_returnsOnlyAuthenticatedAccountsClubAccount() {
        Club club = club();
        ClubAccount account = account(club, 2L, 20L, "Jamie", ClubAccountRole.ADMIN);
        when(clubAccessService.requireClubAccount("club", 20L)).thenReturn(account);

        ClubAccountResponse result = service.getMyAccount("club", 20L);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAccountId()).isEqualTo(20L);
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getAccount_returnsRequestedAccountForOwner() {
        Club club = club();
        ClubAccount account = account(club, 2L, 20L, "Jamie", ClubAccountRole.ADMIN);
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(clubAccountRepository.findByIdAndClub(2L, club)).thenReturn(Optional.of(account));

        ClubAccountResponse result = service.getAccount("club", 2L, 10L);

        assertThat(result.getDisplayName()).isEqualTo("Jamie");
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        return club;
    }

    private static ClubAccount account(Club club, Long id, Long accountId, String displayName, ClubAccountRole role) {
        ClubAccount account = new ClubAccount(club, accountId, displayName, role);
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}
