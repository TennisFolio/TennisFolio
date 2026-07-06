package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubCreateResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubCommandServiceTest {

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    @Mock
    UserRepository userRepository;

    ClubCommandService service;

    @BeforeEach
    void setUp() {
        ClubAccessService accessService = new ClubAccessService(clubRepository, clubMemberRepository);
        service = new ClubCommandService(clubRepository, clubMemberRepository, userRepository, accessService);
    }

    @Test
    void createClub_savesClubAndRegistersCreatorAsAdmin() {
        User creator = user(10L, "Alex Kim", com.tennisfolio.Tennisfolio.user.domain.Gender.MALE);
        Club savedClub = club("club-public-id", "Morning Tennis", 10L);
        when(userRepository.findByIdAndStatus(10L, UserStatus.ACTIVE)).thenReturn(Optional.of(creator));
        when(clubRepository.save(any(Club.class))).thenReturn(savedClub);

        ClubCreateResponse response = service.createClub(
                new ClubCreateRequest(" Morning Tennis ", "Indoor club"),
                10L
        );

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        ArgumentCaptor<ClubMember> memberCaptor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubRepository).save(clubCaptor.capture());
        verify(clubMemberRepository).save(memberCaptor.capture());

        assertThat(response.getPublicId()).isEqualTo("club-public-id");
        assertThat(clubCaptor.getValue().getName()).isEqualTo("Morning Tennis");
        assertThat(memberCaptor.getValue().getClub()).isSameAs(savedClub);
        assertThat(memberCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(memberCaptor.getValue().getName()).isEqualTo("Alex Kim");
        assertThat(memberCaptor.getValue().getGender()).isEqualTo(Gender.MALE);
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(ClubMemberRole.ADMIN);
    }

    @Test
    void createClub_rejectsAnonymousUser() {
        assertThatThrownBy(() -> service.createClub(new ClubCreateRequest("Morning Tennis", null), null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(clubRepository, never()).save(any());
    }

    @Test
    void createClub_rejectsBlankName() {
        when(userRepository.findByIdAndStatus(10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(user(10L, "Alex Kim", com.tennisfolio.Tennisfolio.user.domain.Gender.MALE)));

        assertThatThrownBy(() -> service.createClub(new ClubCreateRequest(" ", null), 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(clubRepository, never()).save(any());
    }

    @Test
    void updateClub_allowsAdminToChangeDetails() {
        Club club = club("club-public-id", "Morning Tennis", 10L);
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));

        service.updateClub("club-public-id", new ClubUpdateRequest("Evening Tennis", "Outdoor club"), 10L);

        assertThat(club.getName()).isEqualTo("Evening Tennis");
        assertThat(club.getDescription()).isEqualTo("Outdoor club");
    }

    @Test
    void deleteClub_rejectsNonAdmin() {
        Club club = club("club-public-id", "Morning Tennis", 10L);
        ClubMember member = member(club, 100L, 11L, "Jamie Lee", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 11L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.deleteClub("club-public-id", 11L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static Club club(String publicId, String name, Long createdByUserId) {
        Club club = new Club(name, null, createdByUserId);
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
        ClubMember member = new ClubMember(club, userId, name, Gender.MALE, role, null, null, null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private static User user(Long userId, String nickName, com.tennisfolio.Tennisfolio.user.domain.Gender gender) {
        return User.builder()
                .userId(userId)
                .email("alex@example.com")
                .nickName(nickName)
                .gender(gender)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
