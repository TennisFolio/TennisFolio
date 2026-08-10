package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberBulkCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
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
import java.util.List;

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

    @Mock
    ClubSkillTierRepository clubSkillTierRepository;

    ClubMemberCommandService service;

    @BeforeEach
    void setUp() {
        ClubAccessService accessService = new ClubAccessService(clubRepository, clubMemberRepository);
        service = new ClubMemberCommandService(
                clubMemberRepository,
                clubSkillTierRepository,
                accessService
        );
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
                new ClubMemberCreateRequest(" Jamie Lee ", "FEMALE", "MEMBER", null, "010", "lefty"),
                10L
        );

        ArgumentCaptor<ClubMember> captor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getClub()).isSameAs(club);
        assertThat(captor.getValue().getUserId()).isNull();
        assertThat(captor.getValue().getName()).isEqualTo("Jamie Lee");
        assertThat(captor.getValue().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(captor.getValue().getRole()).isEqualTo(ClubMemberRole.MEMBER);
        assertThat(captor.getValue().getSkillTier()).isNull();
    }

    @Test
    void addMember_assignsSkillTierFromSameClub() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        ClubSkillTier skillTier = skillTier(club, 1L, "상급", 3);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(false);
        when(clubSkillTierRepository.findByIdAndClub(1L, club)).thenReturn(Optional.of(skillTier));

        service.addMember(
                "club-public-id",
                new ClubMemberCreateRequest("Jamie Lee", "FEMALE", "MEMBER", 1L, "010", "lefty"),
                10L
        );

        ArgumentCaptor<ClubMember> captor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getSkillTier()).isSameAs(skillTier);
    }

    @Test
    void addMember_rejectsSkillTierFromAnotherClub() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(false);
        when(clubSkillTierRepository.findByIdAndClub(2L, club)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMember(
                "club-public-id",
                new ClubMemberCreateRequest("Jamie Lee", "FEMALE", "MEMBER", 2L, null, null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(clubMemberRepository, never()).save(any());
    }

    @Test
    void updateMember_clearsSkillTierWhenSkillTierIdIsNull() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        ClubSkillTier skillTier = skillTier(club, 1L, "상급", 3);
        ClubMember member = new ClubMember(
                club,
                null,
                "Jamie Lee",
                Gender.FEMALE,
                ClubMemberRole.MEMBER,
                skillTier,
                null,
                null
        );
        ReflectionTestUtils.setField(member, "id", 101L);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 101L)).thenReturn(Optional.of(member));

        service.updateMember(
                "club-public-id",
                101L,
                new ClubMemberUpdateRequest("Jamie Lee", "FEMALE", "MEMBER", null, null, null),
                10L
        );

        assertThat(member.getSkillTier()).isNull();
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
    void addMembers_savesAllMembersWhenWholeRequestIsValid() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(false);
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Morgan Park")).thenReturn(false);

        service.addMembers(
                "club-public-id",
                new ClubMemberBulkCreateRequest(List.of(
                        new ClubMemberCreateRequest("Jamie Lee", "FEMALE", "MEMBER", null, null, null),
                        new ClubMemberCreateRequest("Morgan Park", "MALE", "ADMIN", null, null, null)
                )),
                10L
        );

        ArgumentCaptor<Iterable<ClubMember>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(clubMemberRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(ClubMember::getName)
                .containsExactly("Jamie Lee", "Morgan Park");
    }

    @Test
    void addMembers_rejectsDuplicateNamesWithinRequestWithoutSavingAnyMember() {
        Club club = club();
        ClubMember admin = member(club, 100L, 10L, "Alex Kim", ClubMemberRole.ADMIN);
        when(clubRepository.findByPublicIdAndDeletedAtIsNull("club-public-id")).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "Jamie Lee")).thenReturn(false);

        assertThatThrownBy(() -> service.addMembers(
                "club-public-id",
                new ClubMemberBulkCreateRequest(List.of(
                        new ClubMemberCreateRequest("Jamie Lee", "FEMALE", "MEMBER", null, null, null),
                        new ClubMemberCreateRequest(" Jamie Lee ", "MALE", "MEMBER", null, null, null)
                )),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);

        verify(clubMemberRepository, never()).saveAll(any());
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

    private static ClubSkillTier skillTier(Club club, Long id, String name, int level) {
        ClubSkillTier skillTier = new ClubSkillTier(club, name, level);
        ReflectionTestUtils.setField(skillTier, "id", id);
        return skillTier;
    }
}
