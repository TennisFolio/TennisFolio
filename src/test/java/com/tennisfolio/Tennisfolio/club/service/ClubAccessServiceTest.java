package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccessServiceTest {

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    ClubAccessService service;

    @BeforeEach
    void setUp() {
        service = new ClubAccessService(clubRepository, clubMemberRepository);
    }

    @Test
    void requireActiveMemberClub_returnsClubAfterMemberCheck() {
        Club club = club();
        ClubMember member = new ClubMember(club, 10L, "Alex Kim", Gender.MALE, ClubMemberRole.MEMBER, null, null, null);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(member));

        Club response = service.requireActiveMemberClub("club-public-id", 10L);

        assertThat(response).isSameAs(club);
    }

    @Test
    void isActiveAdmin_returnsTrueForActiveAdmin() {
        ClubMember admin = clubMember(10L, ClubMemberRole.ADMIN);
        when(clubMemberRepository.findByClubIdAndUserIdAndActiveTrue(100L, 10L))
                .thenReturn(Optional.of(admin));

        assertThat(service.isActiveAdmin(100L, 10L)).isTrue();
    }

    @Test
    void isActiveAdmin_returnsFalseForMember() {
        ClubMember member = clubMember(10L, ClubMemberRole.MEMBER);
        when(clubMemberRepository.findByClubIdAndUserIdAndActiveTrue(100L, 10L))
                .thenReturn(Optional.of(member));

        assertThat(service.isActiveAdmin(100L, 10L)).isFalse();
    }

    @Test
    void isActiveAdmin_returnsFalseWithoutAuthenticatedUser() {
        assertThat(service.isActiveAdmin(100L, null)).isFalse();

        verifyNoInteractions(clubMemberRepository);
    }

    @Test
    void isActiveAdmin_returnsFalseForInactiveMember() {
        when(clubMemberRepository.findByClubIdAndUserIdAndActiveTrue(100L, 10L))
                .thenReturn(Optional.empty());

        assertThat(service.isActiveAdmin(100L, 10L)).isFalse();
    }

    private static ClubMember clubMember(Long userId, ClubMemberRole role) {
        return new ClubMember(club(), userId, "Alex Kim", Gender.MALE, role, null, null, null);
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        ReflectionTestUtils.setField(club, "publicId", "club-public-id");
        return club;
    }
}
