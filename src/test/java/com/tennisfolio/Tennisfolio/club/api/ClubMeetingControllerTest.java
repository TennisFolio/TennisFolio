package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.service.ClubMeetingCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMeetingQueryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubMeetingControllerTest {

    @Mock
    ClubMeetingCommandService clubMeetingCommandService;

    @Mock
    ClubMeetingQueryService clubMeetingQueryService;

    @InjectMocks
    ClubMeetingController clubMeetingController;

    @Test
    void getClubMeetings_passesCurrentUserToQueryService() {
        Authentication authentication = auth(10L);
        when(clubMeetingQueryService.getClubMeetings("club-public-id", 10L))
                .thenReturn(List.of(summaryResponse()));

        ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> response =
                clubMeetingController.getClubMeetings(authentication, "club-public-id");

        verify(clubMeetingQueryService).getClubMeetings("club-public-id", 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void createClubMeeting_passesCurrentUserToCommandService() {
        Authentication authentication = auth(10L);
        MeetingCreateRequest request = createRequest();
        when(clubMeetingCommandService.createClubMeeting("club-public-id", request, 10L))
                .thenReturn(new MeetingCreateResponse("meeting-public-id"));

        ResponseEntity<ResponseDTO<MeetingCreateResponse>> response =
                clubMeetingController.createClubMeeting(authentication, "club-public-id", request);

        verify(clubMeetingCommandService).createClubMeeting("club-public-id", request, 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void getClubMeeting_passesCurrentUserToQueryService() {
        Authentication authentication = auth(20L);
        when(clubMeetingQueryService.getClubMeeting("club-public-id", "meeting-public-id", 20L))
                .thenReturn(detailResponse(false));

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                clubMeetingController.getClubMeeting(authentication, "club-public-id", "meeting-public-id");

        verify(clubMeetingQueryService).getClubMeeting("club-public-id", "meeting-public-id", 20L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void updateClubMeeting_passesCurrentUserToCommandService() {
        Authentication authentication = auth(20L);
        MeetingUpdateRequest request = updateRequest();
        when(clubMeetingCommandService.updateClubMeeting("club-public-id", "meeting-public-id", request, 20L))
                .thenReturn(detailResponse(false));

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                clubMeetingController.updateClubMeeting(
                        authentication,
                        "club-public-id",
                        "meeting-public-id",
                        request
                );

        verify(clubMeetingCommandService).updateClubMeeting("club-public-id", "meeting-public-id", request, 20L);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void updateClubMeetingStatus_passesCurrentUserToCommandService() {
        Authentication authentication = auth(20L);
        MeetingStatusUpdateRequest request = new MeetingStatusUpdateRequest("CLOSED");
        when(clubMeetingCommandService.updateClubMeetingStatus("club-public-id", "meeting-public-id", request, 20L))
                .thenReturn(detailResponse(false));

        clubMeetingController.updateClubMeetingStatus(authentication, "club-public-id", "meeting-public-id", request);

        verify(clubMeetingCommandService).updateClubMeetingStatus(
                "club-public-id",
                "meeting-public-id",
                request,
                20L
        );
    }

    @Test
    void deleteClubMeeting_passesCurrentUserToCommandService() {
        Authentication authentication = auth(20L);

        ResponseEntity<Void> response =
                clubMeetingController.deleteClubMeeting(authentication, "club-public-id", "meeting-public-id");

        verify(clubMeetingCommandService).deleteClubMeeting("club-public-id", "meeting-public-id", 20L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    private static Authentication auth(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
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

    private static MeetingSummaryResponse summaryResponse() {
        return new MeetingSummaryResponse(
                "meeting-public-id",
                "Saturday doubles",
                LocalDateTime.of(2026, 7, 4, 10, 0),
                LocalDateTime.of(2026, 7, 4, 12, 0),
                2,
                6,
                4L,
                1L,
                0L,
                "OPEN",
                false
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
