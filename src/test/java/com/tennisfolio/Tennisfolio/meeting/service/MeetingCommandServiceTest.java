package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingCommandServiceTest {

    @Mock
    MeetingRepository meetingRepository;

    MeetingCommandService service;

    @BeforeEach
    void setUp() {
        service = new MeetingCommandService(meetingRepository);
    }

    @Test
    void createMeeting_savesMeetingForAuthenticatedOwner() {
        Meeting saved = meeting(10L);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(saved);

        MeetingCreateResponse response = service.createMeeting(createRequest(), 10L);

        verify(meetingRepository).save(any(Meeting.class));
        assertThat(response.getPublicId()).isEqualTo(saved.getPublicId());
    }

    @Test
    void createMeeting_rejectsAnonymousUser() {
        assertThatThrownBy(() -> service.createMeeting(createRequest(), null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(meetingRepository, never()).save(any());
    }

    @Test
    void createMeeting_rejectsEndAtNotAfterStartAt() {
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 12, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6
        );

        assertThatThrownBy(() -> service.createMeeting(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createMeeting_rejectsNonPositiveCourtCountAndTotalGames() {
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                0,
                0
        );

        assertThatThrownBy(() -> service.createMeeting(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createMeeting_rejectsMixedTotalAndGenderCapacity() {
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                12,
                8,
                null,
                2,
                6
        );

        assertThatThrownBy(() -> service.createMeeting(request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(meetingRepository, never()).save(any());
    }

    @Test
    void updateMeeting_rejectsNonOwner() {
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMeeting("meeting-public-id", updateRequest(), 10L))
                .isInstanceOf(com.tennisfolio.Tennisfolio.exception.NotFoundException.class);
    }

    @Test
    void updateMeeting_updatesOwnerMeeting() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        MeetingDetailResponse response = service.updateMeeting("meeting-public-id", updateRequest(), 10L);

        assertThat(response.getTitle()).isEqualTo("Sunday doubles");
        assertThat(response.getCourtCount()).isEqualTo(3);
        assertThat(response.getTotalGames()).isEqualTo(8);
    }

    @Test
    void updateMeeting_rejectsMixedTotalAndGenderCapacity() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));
        MeetingUpdateRequest request = new MeetingUpdateRequest(
                "Sunday doubles",
                LocalDateTime.of(2026, 7, 5, 10, 0),
                LocalDateTime.of(2026, 7, 5, 12, 0),
                null,
                12,
                null,
                4,
                2,
                6
        );

        assertThatThrownBy(() -> service.updateMeeting("meeting-public-id", request, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateMeeting_rejectsScheduleConditionChangeAfterCompetitionCreated() {
        Meeting meeting = meeting(10L);
        meeting.connectCompetition(100L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        assertThatThrownBy(() -> service.updateMeeting("meeting-public-id", updateRequest(), 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateStatus_changesOwnerMeetingStatus() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        MeetingDetailResponse response = service.updateStatus(
                "meeting-public-id",
                new MeetingStatusUpdateRequest("CLOSED"),
                10L
        );

        assertThat(response.getStatus()).isEqualTo("CLOSED");
    }

    @Test
    void deleteMeeting_softDeletesOwnerMeeting() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull("meeting-public-id", 10L))
                .thenReturn(Optional.of(meeting));

        service.deleteMeeting("meeting-public-id", 10L);

        assertThat(meeting.isDeleted()).isTrue();
    }

    private static MeetingCreateRequest createRequest() {
        return new MeetingCreateRequest(
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                "Indoor court",
                12,
                null,
                null,
                2,
                6
        );
    }

    private static MeetingUpdateRequest updateRequest() {
        return new MeetingUpdateRequest(
                "Sunday doubles",
                LocalDateTime.of(2026, 7, 5, 10, 0),
                LocalDateTime.of(2026, 7, 5, 12, 0),
                null,
                null,
                null,
                null,
                3,
                8
        );
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
