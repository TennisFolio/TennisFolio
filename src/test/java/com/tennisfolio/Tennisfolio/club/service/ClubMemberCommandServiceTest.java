package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
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
class ClubMemberCommandServiceTest {

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    ClubMemberCommandService service;

    @BeforeEach
    void setUp() {
        ClubAccessService accessService = new ClubAccessService(clubRepository, clubMemberRepository);
        service = new ClubMemberCommandService(clubMemberRepository, accessService);
    }

    @Test
    void addMember_allowsAdminToAddManualMember() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        ClubMember saved = member(club, 101L, null, "Jamie Lee", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(false);
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(saved);

        service.addMember(
                "club-public-id",
                new ClubMemberCreateRequest(" Jamie Lee ", "FEMALE", "MEMBER", "NTRP 3.5", "010", "lefty"),
                10L
        );

        ArgumentCaptor<ClubMember> captor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getClub()).isSameAs(club);
        assertThat(captor.getValue().getUserId()).isNull();
        assertThat(captor.getValue().getName()).isEqualTo("Jamie Lee");
        assertThat(captor.getValue().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(captor.getValue().getRole()).isEqualTo(ClubMemberRole.MEMBER);
        assertThat(captor.getValue().getSkillNote()).isEqualTo("NTRP 3.5");
    }

    @Test
    void addMember_rejectsDuplicateActiveName() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(true);

        assertThatThrownBy(() -> service.addMember(
                "club-public-id",
                new ClubMemberCreateRequest("Jamie Lee", "MALE", "MEMBER", null, null, null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
        verify(clubMemberRepository, never()).save(any());
    }

    @Test
    void updateMember_rejectsLastAdminDemotion() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 100L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.countByClubAndRoleAndActiveTrue(club, ClubMemberRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateMember(
                "club-public-id",
                100L,
                new ClubMemberUpdateRequest("Alex Kim", "MALE", "MEMBER", null, null, null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteMember_rejectsLastAdminDelete() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 100L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.countByClubAndRoleAndActiveTrue(club, ClubMemberRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteMember("club-public-id", 100L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateMember_rejectsMemberManager() {
        Club club = club();
        ClubMember manager = member(club, 101L, 11L, "Jamie Lee", ClubMemberRole.MEMBER);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 11L)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> service.updateMember(
                "club-public-id",
                100L,
                new ClubMemberUpdateRequest("Alex Kim", "MALE", "ADMIN", null, null, null),
                11L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(club, "publicId", "club-public-id");
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
}
