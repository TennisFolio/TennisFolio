package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccessServiceTest {

    @Mock
    ClubRepository clubRepository;
    @Mock
    ClubAccountRepository clubAccountRepository;

    ClubAccessService service;

    @BeforeEach
    void setUp() {
        service = new ClubAccessService(clubRepository, clubAccountRepository);
    }

    @Test
    void requireOperator_returnsClubForOwner() {
        Club club = club();
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubAccountRepository.findByClubAndAccountId(club, 10L))
                .thenReturn(Optional.of(new ClubAccount(club, 10L, ClubAccountRole.OWNER)));

        assertThat(service.requireOperator("club-public-id", 10L)).isSameAs(club);
    }

    @Test
    void requireOwner_rejectsAdmin() {
        Club club = club();
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubAccountRepository.findByClubAndAccountId(club, 10L))
                .thenReturn(Optional.of(new ClubAccount(club, 10L, ClubAccountRole.ADMIN)));

        assertThatThrownBy(() -> service.requireOwner("club-public-id", 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void isOperator_returnsFalseWithoutAccount() {
        when(clubAccountRepository.findByClubIdAndAccountId(100L, 10L)).thenReturn(Optional.empty());

        assertThat(service.isOperator(100L, 10L)).isFalse();
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        ReflectionTestUtils.setField(club, "publicId", "club-public-id");
        return club;
    }
}
