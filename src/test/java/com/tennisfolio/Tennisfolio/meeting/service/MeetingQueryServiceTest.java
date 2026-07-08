package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingQueryServiceTest {

    @Mock
    MeetingRepository meetingRepository;

    @Mock
    MeetingAttendanceRepository attendanceRepository;

    @Mock
    CompetitionRepository competitionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    ClubMemberRepository clubMemberRepository;

    MeetingQueryService service;

    @BeforeEach
    void setUp() {
        service = new MeetingQueryService(
                meetingRepository,
                attendanceRepository,
                competitionRepository,
                userRepository,
                clubRepository,
                clubMemberRepository
        );
    }

    @Test
    void getMeeting_returnsPublicDetailAndOwnershipFlag() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting))
                .thenReturn(List.of());

        MeetingDetailResponse response = service.getMeeting("meeting-public-id", 10L);

        assertThat(response.getPublicId()).isEqualTo("meeting-public-id");
        assertThat(response.getOwnedByCurrentUser()).isTrue();
        assertThat(response.getCompetitionCreated()).isFalse();
        assertThat(response.getAttendances()).isEmpty();
    }

    @Test
    void getMeeting_returnsCompetitionPublicIdWhenMeetingHasCompetition() {
        Meeting meeting = meeting(10L);
        meeting.connectCompetition(100L);
        Competition competition = competition("competition-public-id");
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(competitionRepository.findByIdAndDeletedAtIsNull(100L))
                .thenReturn(Optional.of(competition));
        when(attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting))
                .thenReturn(List.of());

        MeetingDetailResponse response = service.getMeeting("meeting-public-id", 10L);

        assertThat(response.getCompetitionCreated()).isTrue();
        assertThat(response.getCompetitionPublicId()).isEqualTo("competition-public-id");
    }

    @Test
    void getMeeting_returnsClubNameWhenMeetingBelongsToClub() {
        Meeting meeting = meeting(10L);
        meeting.connectClub(100L);
        Club club = new Club("서초 테니스 크루", null, 10L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(100L))
                .thenReturn(Optional.of(club));
        when(attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting))
                .thenReturn(List.of());

        MeetingDetailResponse response = service.getMeeting("meeting-public-id", 10L);

        assertThat(response.getClubMeeting()).isTrue();
        assertThat(response.getClubName()).isEqualTo("서초 테니스 크루");
    }

    @Test
    void getMeeting_returnsCurrentClubMemberForLoggedInMember() {
        Club club = club(50L, "서초 테니스 크루");
        ClubMember member = new ClubMember(
                club,
                10L,
                "Jamie Lee",
                com.tennisfolio.Tennisfolio.meeting.domain.Gender.FEMALE,
                ClubMemberRole.MEMBER,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(member, "id", 100L);
        Meeting meeting = meeting(20L);
        meeting.connectClub(50L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting-public-id"))
                .thenReturn(Optional.of(meeting));
        when(clubRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, 10L))
                .thenReturn(Optional.of(member));
        when(attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting))
                .thenReturn(List.of());

        MeetingDetailResponse response = service.getMeeting("meeting-public-id", 10L);

        assertThat(response.getCurrentClubMemberId()).isEqualTo(100L);
        assertThat(response.getCurrentClubMemberName()).isEqualTo("Jamie Lee");
        assertThat(response.getCurrentClubMemberGender()).isEqualTo("FEMALE");
    }

    @Test
    void getMeeting_throwsNotFoundForDeletedOrMissingMeeting() {
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMeeting("missing", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnedMeetings_returnsOwnerMeetings() {
        when(meetingRepository.findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(10L))
                .thenReturn(List.of(meeting(10L)));
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.ATTENDING)
        )).thenReturn(10L);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.WAITING)
        )).thenReturn(3L);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.NOT_ATTENDING)
        )).thenReturn(2L);

        List<MeetingSummaryResponse> response = service.getOwnedMeetings(10L);

        assertThat(response)
                .extracting(MeetingSummaryResponse::getPublicId)
                .containsExactly("meeting-public-id");
        assertThat(response.get(0).getCourtCount()).isEqualTo(2);
        assertThat(response.get(0).getTotalGames()).isEqualTo(6);
        assertThat(response.get(0).getAttendingCount()).isEqualTo(10L);
        assertThat(response.get(0).getWaitingCount()).isEqualTo(3L);
        assertThat(response.get(0).getNotAttendingCount()).isEqualTo(2L);
    }

    @Test
    void getClubMeetings_returnsClubMeetingSummaries() {
        Meeting meeting = meeting(10L);
        meeting.connectClub(100L);
        when(meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(100L))
                .thenReturn(List.of(meeting));
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.ATTENDING)
        )).thenReturn(4L);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.WAITING)
        )).thenReturn(1L);
        when(attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                org.mockito.ArgumentMatchers.any(Meeting.class),
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.NOT_ATTENDING)
        )).thenReturn(0L);

        List<MeetingSummaryResponse> response = service.getClubMeetings(100L);

        assertThat(response)
                .extracting(MeetingSummaryResponse::getPublicId)
                .containsExactly("meeting-public-id");
        assertThat(response.get(0).getAttendingCount()).isEqualTo(4L);
        assertThat(response.get(0).getWaitingCount()).isEqualTo(1L);
        assertThat(response.get(0).getNotAttendingCount()).isZero();
    }

    @Test
    void findActiveClubMeetings_returnsClubMeetingDomains() {
        Meeting meeting = meeting(10L);
        meeting.connectClub(100L);
        when(meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(100L))
                .thenReturn(List.of(meeting));

        List<Meeting> response = service.findActiveClubMeetings(100L);

        assertThat(response).containsExactly(meeting);
    }

    @Test
    void findActiveClubMeeting_returnsDomainOnlyWhenMeetingBelongsToClub() {
        Meeting meeting = meeting(10L);
        meeting.connectClub(100L);
        when(meetingRepository.findByPublicIdAndClubIdAndDeletedAtIsNull("meeting-public-id", 100L))
                .thenReturn(Optional.of(meeting));

        Meeting response = service.findActiveClubMeeting("meeting-public-id", 100L);

        assertThat(response).isSameAs(meeting);
        assertThat(response.belongsToClub(100L)).isTrue();
    }

    @Test
    void findActiveClubMeeting_throwsNotFoundWhenMeetingDoesNotBelongToClub() {
        when(meetingRepository.findByPublicIdAndClubIdAndDeletedAtIsNull("meeting-public-id", 100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findActiveClubMeeting("meeting-public-id", 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void toDetailResponse_mapsDomainToApiResponse() {
        Meeting meeting = meeting(10L);
        when(attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting))
                .thenReturn(List.of());

        MeetingDetailResponse response = service.toDetailResponse(meeting, 20L);

        assertThat(response.getPublicId()).isEqualTo("meeting-public-id");
        assertThat(response.getOwnedByCurrentUser()).isFalse();
    }

    private static Meeting meeting(Long ownerUserId) {
        Meeting meeting = new Meeting(
                ownerUserId,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        );
        ReflectionTestUtils.setField(meeting, "publicId", "meeting-public-id");
        return meeting;
    }

    private static Competition competition(String publicId) {
        Competition competition = new Competition(
                "Saturday doubles",
                2,
                2,
                2,
                6,
                1L,
                Competition.CompetitionMode.FIXED_SCHEDULE,
                10L
        );
        ReflectionTestUtils.setField(competition, "publicId", publicId);
        return competition;
    }

    private static Club club(Long id, String name) {
        Club club = new Club(name, null, 10L);
        ReflectionTestUtils.setField(club, "id", id);
        return club;
    }
}
