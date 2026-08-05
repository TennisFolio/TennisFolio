package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingParticipantType;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingAttendanceCommandServiceTest {

    @Mock
    MeetingRepository meetingRepository;

    @Mock
    MeetingAttendanceRepository attendanceRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    @Mock
    ClubSkillTierRepository clubSkillTierRepository;

    MeetingAttendanceCommandService service;

    @BeforeEach
    void setUp() {
        service = new MeetingAttendanceCommandService(
                meetingRepository,
                attendanceRepository,
                userRepository,
                clubRepository,
                clubMemberRepository,
                clubSkillTierRepository
        );
    }

    @Test
    void upsertAttendance_createsAnonymousAttendance() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance saved = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.ATTENDING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class))).thenReturn(saved);

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        );

        verify(attendanceRepository).save(any(MeetingAttendance.class));
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void upsertAttendance_treatsLegacyMaybeStatusAsWaiting() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "MAYBE"),
                null
        );

        assertThat(response.getAttendanceStatus()).isEqualTo("WAITING");
    }

    @Test
    void upsertAttendance_usesAuthenticatedUserProfileInsteadOfRequestIdentity() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(userRepository.findByIdAndStatus(10L, com.tennisfolio.Tennisfolio.common.UserStatus.ACTIVE))
                .thenReturn(Optional.of(user(10L, "Alex Kim", com.tennisfolio.Tennisfolio.user.domain.Gender.MALE)));
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Wrong Name", "FEMALE", "ATTENDING"),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(response.getGender()).isEqualTo("MALE");
    }

    @Test
    void upsertAttendance_updatesOnlyLoggedInUsersOwnAttendanceWithStoredIdentity() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        attendance.assignUser(10L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndUserIdAndDeletedAtIsNull(meeting, 10L))
                .thenReturn(Optional.of(attendance));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Wrong Name", "FEMALE", "ATTENDING"),
                10L
        );

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void upsertAttendance_rejectsLoggedInUserUpdatingAnotherAttendance() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance ownAttendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        ownAttendance.assignUser(10L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndUserIdAndDeletedAtIsNull(meeting, 10L))
                .thenReturn(Optional.of(ownAttendance));

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(200L, "Wrong Name", "FEMALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void upsertAttendance_updatesExistingPublicAttendance() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(100L, "Alex Kim", "FEMALE", "NOT_ATTENDING"),
                null
        );

        assertThat(response.getParticipantName()).isEqualTo("Alex Kim");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getAttendanceStatus()).isEqualTo("NOT_ATTENDING");
    }

    @Test
    void upsertAttendance_rejectsDuplicateActiveName() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).isEqualTo("이미 같은 이름으로 참석 응답이 등록되었습니다.");
                });
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void upsertAttendance_rejectsClosedMeeting() {
        Meeting meeting = meeting(null, null);
        meeting.updateStatus(MeetingStatus.CLOSED);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).isEqualTo("참석 체크가 마감되었습니다.");
                });
    }

    @Test
    void upsertAttendance_rejectsMeetingWithCompetition() {
        Meeting meeting = meeting(null, 200L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).isEqualTo("이미 경기표가 생성된 모임입니다.");
                });
    }

    @Test
    void upsertAttendance_rejectsMissingNameOrGender() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, " ", null, "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void upsertAttendance_rejectsTotalCapacityExceeded() {
        Meeting meeting = meeting(1, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(false);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).isEqualTo("참석 가능 인원이 모두 찼습니다.");
                });
    }

    @Test
    void upsertAttendance_rejectsGenderCapacityExceeded() {
        Meeting meeting = meetingWithCapacities(null, 1, null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(false);
        when(attendanceRepository.countByMeetingAndGenderAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                Gender.MALE,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).isEqualTo("해당 성별의 참석 가능 인원이 모두 찼습니다.");
                });
    }

    @Test
    void upsertAttendance_usesTotalCapacityWhenTotalAndGenderCapacityBothExist() {
        Meeting meeting = meetingWithCapacities(2, 1, null, null);
        MeetingAttendance saved = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.ATTENDING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Alex Kim"))
                .thenReturn(false);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);
        when(attendanceRepository.save(any(MeetingAttendance.class))).thenReturn(saved);

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                null
        );

        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void upsertAttendance_linksLoggedInClubMember() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember member = clubMember(club, 100L, 10L, "Jamie Lee", Gender.FEMALE);
        ClubSkillTier skillTier = clubSkillTier(club, 300L, "A", 3);
        ReflectionTestUtils.setField(member, "skillTier", skillTier);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L))
                .thenReturn(Optional.of(member));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Jamie Lee"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> {
                    MeetingAttendance attendance = invocation.getArgument(0);
                    ReflectionTestUtils.setField(attendance, "id", 100L);
                    return attendance;
                });

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Wrong Name", "MALE", "ATTENDING"),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("Jamie Lee");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getParticipantType()).isEqualTo("CLUB_MEMBER");
        assertThat(response.getClubMemberId()).isEqualTo(100L);
        assertThat(response.getBadgeLabel()).isEqualTo("클럽원");
        assertThat(response.getClubSkillTierId()).isEqualTo(300L);
        assertThat(response.getClubSkillTierName()).isEqualTo("A");
    }

    @Test
    void upsertAttendance_linksExactClubMemberMatchForGuestInput() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember member = clubMember(club, 100L, null, "Jamie Lee", Gender.FEMALE);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndNameAndGenderAndActiveTrueOrderByIdAsc(
                club,
                "Jamie Lee",
                Gender.FEMALE
        )).thenReturn(List.of(member));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Jamie Lee"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Jamie Lee", "FEMALE", "ATTENDING"),
                null
        );

        assertThat(response.getParticipantType()).isEqualTo("CLUB_MEMBER");
        assertThat(response.getClubMemberId()).isEqualTo(100L);
        assertThat(response.getBadgeLabel()).isEqualTo("클럽원");
    }

    @Test
    void upsertAttendance_savesGuestWhenClubMemberMatchIsAmbiguous() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndNameAndGenderAndActiveTrueOrderByIdAsc(
                club,
                "Jamie Lee",
                Gender.FEMALE
        )).thenReturn(java.util.List.of(
                clubMember(club, 100L, null, "Jamie Lee", Gender.FEMALE),
                clubMember(club, 101L, null, "Jamie Lee", Gender.FEMALE)
        ));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "Jamie Lee"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.upsertAttendance(
                "meeting-public-id",
                new MeetingAttendanceUpsertRequest(null, "Jamie Lee", "FEMALE", "ATTENDING"),
                null
        );

        assertThat(response.getParticipantType()).isEqualTo("GUEST");
        assertThat(response.getClubMemberId()).isNull();
        assertThat(response.getBadgeLabel()).isEqualTo("게스트");
    }

    @Test
    void addManagedParticipant_allowsPersonalMeetingOwnerToAddGuest() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance saved = attendance(meeting, 100L, "김테니스", Gender.MALE, AttendanceStatus.ATTENDING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "김테니스"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class))).thenReturn(saved);

        MeetingAttendanceResponse response = service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "MALE", "ATTENDING"),
                10L
        );

        verify(attendanceRepository).save(any(MeetingAttendance.class));
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
        assertThat(response.getBadgeLabel()).isEqualTo("게스트");
    }

    @Test
    void addManagedParticipant_allowsClubAdminToAddClubMemberAsWaiting() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        ClubMember member = clubMember(club, 100L, 20L, "김테니스", Gender.FEMALE);
        ClubSkillTier skillTier = clubSkillTier(club, 300L, "A", 3);
        ReflectionTestUtils.setField(member, "skillTier", skillTier);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 100L)).thenReturn(Optional.of(member));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "김테니스"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(100L, null, null, "WAITING"),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("김테니스");
        assertThat(response.getAttendanceStatus()).isEqualTo("WAITING");
        assertThat(response.getBadgeLabel()).isEqualTo("클럽원");
        assertThat(response.getClubSkillTierId()).isEqualTo(300L);
        assertThat(response.getClubSkillTierName()).isEqualTo("A");
    }

    @Test
    void addManagedParticipant_savesClubGuestSkillTier() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ClubSkillTier skillTier = clubSkillTier(club, 300L, "A", 3);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubSkillTierRepository.findByIdAndClub(300L, club)).thenReturn(Optional.of(skillTier));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "김테니스"))
                .thenReturn(false);
        when(attendanceRepository.save(any(MeetingAttendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAttendanceResponse response = service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "FEMALE", "ATTENDING", 300L),
                10L
        );

        assertThat(response.getClubSkillTierId()).isEqualTo(300L);
        assertThat(response.getClubSkillTierName()).isEqualTo("A");
    }

    @Test
    void addManagedParticipant_rejectsGuestWhoseNameMatchesActiveClubMember() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "CC")).thenReturn(true);

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "CC", "FEMALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void addManagedParticipant_rejectsSkillTierForPersonalMeetingGuest() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "FEMALE", "ATTENDING", 300L),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addManagedParticipant_rejectsUnknownOrForeignClubGuestSkillTier() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubSkillTierRepository.findByIdAndClub(300L, club)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "FEMALE", "ATTENDING", 300L),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void meetingAttendanceResponse_usesResolvedCurrentSkillTierName() {
        MeetingAttendance attendance = attendance(
                meeting(null, null),
                200L,
                "김테니스",
                Gender.FEMALE,
                AttendanceStatus.ATTENDING
        );
        ReflectionTestUtils.setField(attendance, "clubSkillTierId", 300L);

        MeetingAttendanceResponse response = MeetingAttendanceResponse.from(
                attendance,
                Map.of(300L, "새 A")
        );

        assertThat(response.getClubSkillTierId()).isEqualTo(300L);
        assertThat(response.getClubSkillTierName()).isEqualTo("새 A");
    }

    @Test
    void addManagedParticipant_promotesMatchingGuestToClubMember() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        ClubMember member = clubMember(club, 100L, 20L, "김테니스", Gender.FEMALE);
        MeetingAttendance guest = attendance(meeting, 200L, "김테니스", Gender.FEMALE, AttendanceStatus.ATTENDING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 100L)).thenReturn(Optional.of(member));
        when(attendanceRepository.findByMeetingAndParticipantNameAndGenderAndDeletedAtIsNull(
                meeting,
                "김테니스",
                Gender.FEMALE
        )).thenReturn(Optional.of(guest));

        MeetingAttendanceResponse response = service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(100L, null, null, "WAITING"),
                10L
        );

        verify(attendanceRepository, never()).save(any(MeetingAttendance.class));
        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
        assertThat(response.getParticipantType()).isEqualTo("CLUB_MEMBER");
        assertThat(response.getClubMemberId()).isEqualTo(100L);
    }

    @Test
    void addManagedParticipant_rejectsNonOwnerForPersonalMeeting() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "MALE", "ATTENDING"),
                20L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void addManagedParticipant_rejectsNonAdminForClubMeeting() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember member = clubMember(club, 20L, 20L, "일반회원", Gender.MALE);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 20L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "MALE", "ATTENDING"),
                20L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void addManagedParticipant_rejectsMemberFromAnotherClub() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(clubMemberRepository.findByClubAndIdAndActiveTrue(club, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(100L, null, null, "WAITING"),
                10L
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addManagedParticipant_rejectsDuplicateName() {
        Meeting meeting = meeting(null, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "김테니스"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "MALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void addManagedParticipant_rejectsTotalCapacityExceeded() {
        Meeting meeting = meeting(1, null);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, "김테니스"))
                .thenReturn(false);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.addManagedParticipant(
                "meeting-public-id",
                new ManagedMeetingParticipantCreateRequest(null, "김테니스", "MALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateManagedParticipant_allowsOwnerToUpdatePersonalMeetingGuest() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
                meeting,
                "Jamie Lee",
                100L
        )).thenReturn(false);

        MeetingAttendanceResponse response = service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("Jamie Lee", "FEMALE", "ATTENDING", null),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("Jamie Lee");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void updateManagedParticipant_allowsClubAdminToUpdateClubMemberStatusOnly() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        MeetingAttendance attendance = attendance(meeting, 100L, "클럽원", Gender.FEMALE, AttendanceStatus.WAITING);
        attendance.assignParticipant(MeetingParticipantType.CLUB_MEMBER, 200L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        MeetingAttendanceResponse response = service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest(null, null, "ATTENDING", null),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("클럽원");
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void updateManagedParticipant_rejectsGuestWhoseNameMatchesActiveClubMember() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        MeetingAttendance attendance = attendance(meeting, 100L, "게스트", Gender.FEMALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(clubMemberRepository.existsByClubAndNameAndActiveTrue(club, "CC")).thenReturn(true);

        assertThatThrownBy(() -> service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("CC", "FEMALE", "ATTENDING", null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateManagedParticipant_rejectsClubMemberIdentityChange() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        MeetingAttendance attendance = attendance(meeting, 100L, "클럽원", Gender.FEMALE, AttendanceStatus.WAITING);
        attendance.assignParticipant(MeetingParticipantType.CLUB_MEMBER, 200L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("다른 이름", null, "ATTENDING", null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateManagedParticipant_updatesClubGuestSkillTier() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ClubSkillTier skillTier = clubSkillTier(club, 300L, "A", 1);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        MeetingAttendance attendance = attendance(meeting, 100L, "김테니스", Gender.FEMALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
                meeting,
                "김테니스",
                100L
        )).thenReturn(false);
        when(clubSkillTierRepository.findByIdAndClub(300L, club)).thenReturn(Optional.of(skillTier));

        MeetingAttendanceResponse response = service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("김테니스", "FEMALE", "ATTENDING", 300L),
                10L
        );

        assertThat(response.getClubSkillTierId()).isEqualTo(300L);
        assertThat(response.getClubSkillTierName()).isEqualTo("A");
    }

    @Test
    void updateManagedParticipant_rejectsSkillTierForPersonalMeetingGuest() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "김테니스", Gender.FEMALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
                meeting,
                "김테니스",
                100L
        )).thenReturn(false);

        assertThatThrownBy(() -> service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("김테니스", "FEMALE", "ATTENDING", 300L),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateManagedParticipant_rejectsCapacityExceededWhenGuestBecomesAttending() {
        Meeting meeting = meeting(1, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "김테니스", Gender.FEMALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
                meeting,
                "김테니스",
                100L
        )).thenReturn(false);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.updateManagedParticipant(
                "meeting-public-id",
                100L,
                new ManagedMeetingParticipantUpdateRequest("김테니스", "FEMALE", "ATTENDING", null),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateAttendance_allowsOwnerToEditAttendance() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        MeetingAttendanceResponse response = service.updateAttendance(
                "meeting-public-id",
                100L,
                new MeetingAttendanceUpsertRequest(null, "Jamie Lee", "FEMALE", "ATTENDING"),
                10L
        );

        assertThat(response.getParticipantName()).isEqualTo("Jamie Lee");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getAttendanceStatus()).isEqualTo("ATTENDING");
    }

    @Test
    void updateAttendance_rejectsOwnerChangingAccountLinkedAttendanceIdentity() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        attendance.assignUser(20L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> service.updateAttendance(
                "meeting-public-id",
                100L,
                new MeetingAttendanceUpsertRequest(null, "Jamie Lee", "FEMALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateAttendance_rejectsCapacityExceededForOwnerEdit() {
        Meeting meeting = meeting(1, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        )).thenReturn(1L);

        assertThatThrownBy(() -> service.updateAttendance(
                "meeting-public-id",
                100L,
                new MeetingAttendanceUpsertRequest(null, "Alex Kim", "MALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateAttendance_rejectsNonOwner() {
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate("meeting-public-id", 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAttendance(
                "meeting-public-id",
                100L,
                new MeetingAttendanceUpsertRequest(null, "Jamie Lee", "FEMALE", "ATTENDING"),
                10L
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteAttendance_softDeletesOwnerAttendance() {
        Meeting meeting = meeting(null, null);
        MeetingAttendance attendance = attendance(meeting, 100L, "Alex Kim", Gender.MALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        service.deleteAttendance("meeting-public-id", 100L, 10L);

        assertThat(attendance.isDeleted()).isTrue();
    }

    @Test
    void deleteAttendance_allowsClubAdminToDeleteParticipant() {
        Club club = club(50L);
        Meeting meeting = clubMeeting(club.getId());
        ClubMember admin = clubMember(club, 10L, 10L, "관리자", Gender.MALE);
        ReflectionTestUtils.setField(admin, "role", ClubMemberRole.ADMIN);
        MeetingAttendance attendance = attendance(meeting, 100L, "김테니스", Gender.FEMALE, AttendanceStatus.WAITING);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L)).thenReturn(Optional.of(admin));
        when(attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(100L, meeting))
                .thenReturn(Optional.of(attendance));

        service.deleteAttendance("meeting-public-id", 100L, 10L);

        assertThat(attendance.isDeleted()).isTrue();
    }

    private static Meeting meeting(Integer maxParticipants, Long competitionId) {
        return meetingWithCapacities(maxParticipants, null, null, competitionId);
    }

    private static Meeting meetingWithCapacities(
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants,
            Long competitionId
    ) {
        Meeting meeting = new Meeting(
                10L,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                maxParticipants,
                maxMaleParticipants,
                maxFemaleParticipants,
                2,
                6
        );
        ReflectionTestUtils.setField(meeting, "publicId", "meeting-public-id");
        if (competitionId != null) {
            meeting.connectCompetition(competitionId);
        }
        return meeting;
    }

    private static Meeting clubMeeting(Long clubId) {
        Meeting meeting = meeting(null, null);
        meeting.connectClub(clubId);
        return meeting;
    }

    private static Club club(Long id) {
        Club club = new Club("테니스 클럽", null, 10L);
        ReflectionTestUtils.setField(club, "id", id);
        return club;
    }

    private static User user(Long id, String nickName, com.tennisfolio.Tennisfolio.user.domain.Gender gender) {
        return User.builder()
                .userId(id)
                .email("user@example.com")
                .nickName(nickName)
                .gender(gender)
                .status(com.tennisfolio.Tennisfolio.common.UserStatus.ACTIVE)
                .build();
    }

    private static ClubMember clubMember(
            Club club,
            Long id,
            Long userId,
            String name,
            Gender gender
    ) {
        ClubMember member = new ClubMember(
                club,
                userId,
                name,
                gender,
                ClubMemberRole.MEMBER,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private static ClubSkillTier clubSkillTier(Club club, Long id, String name, int level) {
        ClubSkillTier skillTier = new ClubSkillTier(club, name, level);
        ReflectionTestUtils.setField(skillTier, "id", id);
        return skillTier;
    }

    private static MeetingAttendance attendance(
            Meeting meeting,
            Long id,
            String participantName,
            Gender gender,
            AttendanceStatus status
    ) {
        MeetingAttendance attendance = new MeetingAttendance(meeting, participantName, gender, status);
        ReflectionTestUtils.setField(attendance, "id", id);
        return attendance;
    }
}
