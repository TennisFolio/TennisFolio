package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDetailResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubSummaryResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubQueryServiceTest {

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    ClubQueryService service;

    @BeforeEach
    void setUp() {
        ClubAccessService accessService = new ClubAccessService(clubRepository, clubMemberRepository);
        service = new ClubQueryService(clubMemberRepository, accessService);
    }

    @Test
    void getMyClubs_returnsActiveClubsLinkedToCurrentUser() {
        Club club = club("club-public-id", "Morning Tennis");
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubMemberRepository.findByUserIdAndActiveTrueOrderByIdAsc(10L)).thenReturn(List.of(admin));
        when(clubMemberRepository.countByClubAndActiveTrue(club)).thenReturn(3L);

        List<ClubSummaryResponse> response = service.getMyClubs(10L);

        assertThat(response).extracting(ClubSummaryResponse::getPublicId).containsExactly("club-public-id");
        assertThat(response.get(0).getName()).isEqualTo("Morning Tennis");
        assertThat(response.get(0).getCurrentUserRole()).isEqualTo("ADMIN");
        assertThat(response.get(0).getMemberCount()).isEqualTo(3L);
    }

    @Test
    void getMyClubs_rejectsAnonymousUser() {
        assertThatThrownBy(() -> service.getMyClubs(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getClub_returnsDetailForActiveMember() {
        Club club = club("club-public-id", "Morning Tennis");
        ClubMember member = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(member));
        when(clubMemberRepository.countByClubAndActiveTrue(club)).thenReturn(2L);

        ClubDetailResponse response = service.getClub("club-public-id", 10L);

        assertThat(response.getPublicId()).isEqualTo("club-public-id");
        assertThat(response.getName()).isEqualTo("Morning Tennis");
        assertThat(response.getCurrentUserRole()).isEqualTo("MEMBER");
        assertThat(response.getAdmin()).isFalse();
        assertThat(response.getMemberCount()).isEqualTo(2L);
    }

    @Test
    void getClub_rejectsNonMember() {
        Club club = club("club-public-id", "Morning Tennis");
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClub("club-public-id", 11L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getMembers_returnsActiveMembersForMember() {
        Club club = club("club-public-id", "Morning Tennis");
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        ClubMember member = member(club, 101L, null, "Jamie Lee", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndActiveTrueOrderByNameAscIdAsc(club))
                .thenReturn(List.of(admin, member));

        List<ClubMemberResponse> response = service.getMembers("club-public-id", null, 10L);

        assertThat(response).extracting(ClubMemberResponse::getName).containsExactly("Alex Kim", "Jamie Lee");
        assertThat(response.get(0).getUserId()).isEqualTo(10L);
        assertThat(response.get(1).getGender()).isEqualTo("MALE");
    }

    @Test
    void getMembers_filtersByKeyword() {
        Club club = club("club-public-id", "Morning Tennis");
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        ClubMember member = member(club, 101L, null, "Jamie Lee", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndNameContainingIgnoreCaseAndActiveTrueOrderByNameAscIdAsc(
                club,
                "jamie"
        )).thenReturn(List.of(member));

        List<ClubMemberResponse> response = service.getMembers("club-public-id", " jamie ", 10L);

        assertThat(response).extracting(ClubMemberResponse::getName).containsExactly("Jamie Lee");
    }

    private static Club club(String publicId, String name) {
        Club club = new Club(name, "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "publicId", publicId);
        return club;
    }

    private static ClubMember member(
            Club club,
            Long id,
            Long userId,
            String name,
            ClubMemberRole role
    ) {
        ClubMember member = new ClubMember(club, userId, name, Gender.MALE, role, "NTRP 3.5", "010", "memo");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
