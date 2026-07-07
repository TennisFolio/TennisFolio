package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubMeetingCommandServiceTest {

    @Mock
    ClubAccessService clubAccessService;

    @Mock
    MeetingCommandService meetingCommandService;

    @InjectMocks
    ClubMeetingCommandService service;

    @Test
    void createClubMeeting_checksAdminAndCreatesMeetingForClub() {
        Club club = club(100L);
        MeetingCreateRequest request = createRequest();
        when(clubAccessService.requireAdmin("club-public-id", 10L)).thenReturn(club);
        when(meetingCommandService.createClubMeeting(request, 10L, 100L))
                .thenReturn(new MeetingCreateResponse("meeting-public-id"));

        MeetingCreateResponse response = service.createClubMeeting("club-public-id", request, 10L);

        verify(clubAccessService).requireAdmin("club-public-id", 10L);
        verify(meetingCommandService).createClubMeeting(request, 10L, 100L);
        assertThat(response.getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void updateClubMeeting_checksAdminAndUpdatesMeetingForClub() {
        Club club = club(100L);
        MeetingUpdateRequest request = updateRequest();
        when(clubAccessService.requireAdmin("club-public-id", 20L)).thenReturn(club);
        when(meetingCommandService.updateClubMeeting("meeting-public-id", 100L, request, 20L))
                .thenReturn(detailResponse(false));

        MeetingDetailResponse response = service.updateClubMeeting(
                "club-public-id",
                "meeting-public-id",
                request,
                20L
        );

        verify(meetingCommandService).updateClubMeeting("meeting-public-id", 100L, request, 20L);
        assertThat(response.getOwnedByCurrentUser()).isFalse();
    }

    @Test
    void updateClubMeetingStatus_checksAdminAndUpdatesStatusForClub() {
        Club club = club(100L);
        MeetingStatusUpdateRequest request = new MeetingStatusUpdateRequest("CLOSED");
        when(clubAccessService.requireAdmin("club-public-id", 20L)).thenReturn(club);
        when(meetingCommandService.updateClubMeetingStatus("meeting-public-id", 100L, request, 20L))
                .thenReturn(detailResponse(false));

        service.updateClubMeetingStatus("club-public-id", "meeting-public-id", request, 20L);

        verify(meetingCommandService).updateClubMeetingStatus("meeting-public-id", 100L, request, 20L);
    }

    @Test
    void deleteClubMeeting_checksAdminAndDeletesMeetingForClub() {
        Club club = club(100L);
        when(clubAccessService.requireAdmin("club-public-id", 20L)).thenReturn(club);

        service.deleteClubMeeting("club-public-id", "meeting-public-id", 20L);

        verify(meetingCommandService).deleteClubMeeting("meeting-public-id", 100L);
    }

    private static Club club(Long id) {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", id);
        return club;
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
                2,
                6
        );
    }

    private static MeetingDetailResponse detailResponse(boolean ownedByCurrentUser) {
        return new MeetingDetailResponse(
                "meeting-public-id",
                null,
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                null,
                null,
                null,
                null,
                2,
                6,
                "OPEN",
                ownedByCurrentUser,
                null,
                false,
                List.of()
        );
    }
}
