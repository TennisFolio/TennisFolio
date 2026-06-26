package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
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

    MeetingQueryService service;

    @BeforeEach
    void setUp() {
        service = new MeetingQueryService(meetingRepository);
    }

    @Test
    void getMeeting_returnsPublicDetailAndOwnershipFlag() {
        Meeting meeting = meeting(10L);
        when(meetingRepository.findByPublicIdAndDeletedAtIsNull("meeting-public-id"))
                .thenReturn(Optional.of(meeting));

        MeetingDetailResponse response = service.getMeeting("meeting-public-id", 10L);

        assertThat(response.getPublicId()).isEqualTo("meeting-public-id");
        assertThat(response.getOwnedByCurrentUser()).isTrue();
        assertThat(response.getCompetitionCreated()).isFalse();
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

        List<MeetingSummaryResponse> response = service.getOwnedMeetings(10L);

        assertThat(response)
                .extracting(MeetingSummaryResponse::getPublicId)
                .containsExactly("meeting-public-id");
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
