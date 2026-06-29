package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
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

    MeetingQueryService service;

    @BeforeEach
    void setUp() {
        service = new MeetingQueryService(meetingRepository, attendanceRepository);
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
    void getMeeting_throwsNotFoundForDeletedOrMissingMeeting() {
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMeeting("missing", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getManagedMeeting_rejectsNonOwner() {
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getManagedMeeting("meeting-public-id", 10L))
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
                org.mockito.ArgumentMatchers.eq(AttendanceStatus.MAYBE)
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
        assertThat(response.get(0).getMaybeCount()).isEqualTo(3L);
        assertThat(response.get(0).getNotAttendingCount()).isEqualTo(2L);
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
}
