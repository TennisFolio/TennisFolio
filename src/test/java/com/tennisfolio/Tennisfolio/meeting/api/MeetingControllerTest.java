package com.tennisfolio.Tennisfolio.meeting.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCompetitionCreateService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
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
class MeetingControllerTest {

    @Mock
    MeetingCommandService meetingCommandService;

    @Mock
    MeetingQueryService meetingQueryService;

    @Mock
    MeetingAttendanceCommandService attendanceCommandService;

    @Mock
    MeetingCompetitionCreateService competitionCreateService;

    @InjectMocks
    MeetingController meetingController;

    @Test
    void createMeeting_passesCurrentUserToCommandService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        MeetingCreateRequest request = createRequest();
        when(meetingCommandService.createMeeting(request, 10L))
                .thenReturn(new MeetingCreateResponse("meeting-public-id"));

        ResponseEntity<ResponseDTO<MeetingCreateResponse>> response =
                meetingController.createMeeting(authentication, request);

        verify(meetingCommandService).createMeeting(request, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void getMeeting_returnsPublicDetailWithoutAuthentication() {
        MeetingDetailResponse detail = detailResponse("meeting-public-id", false);
        when(meetingQueryService.getMeeting("meeting-public-id", null)).thenReturn(detail);

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                meetingController.getMeeting(null, "meeting-public-id");

        verify(meetingQueryService).getMeeting("meeting-public-id", null);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void getMyMeetings_returnsCurrentUsersMeetings() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        when(meetingQueryService.getOwnedMeetings(10L))
                .thenReturn(List.of(new MeetingSummaryResponse(
                        "meeting-public-id",
                        "Saturday doubles",
                        LocalDateTime.of(2026, 7, 4, 10, 0),
                        LocalDateTime.of(2026, 7, 4, 12, 0),
                        2,
                        6,
                        10L,
                        3L,
                        2L,
                        "OPEN",
                        false
                )));

        ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> response =
                meetingController.getMyMeetings(authentication);

        verify(meetingQueryService).getOwnedMeetings(10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void updateMeeting_passesOwnerUserIdToCommandService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        MeetingUpdateRequest request = updateRequest();
        MeetingDetailResponse detail = detailResponse("meeting-public-id", true);
        when(meetingCommandService.updateMeeting("meeting-public-id", request, 10L)).thenReturn(detail);

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                meetingController.updateMeeting(authentication, "meeting-public-id", request);

        verify(meetingCommandService).updateMeeting("meeting-public-id", request, 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getOwnedByCurrentUser()).isTrue();
    }

    @Test
    void updateMeetingStatus_passesOwnerUserIdToCommandService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        MeetingStatusUpdateRequest request = new MeetingStatusUpdateRequest("CLOSED");
        when(meetingCommandService.updateStatus("meeting-public-id", request, 10L))
                .thenReturn(detailResponse("meeting-public-id", true));

        meetingController.updateMeetingStatus(authentication, "meeting-public-id", request);

        verify(meetingCommandService).updateStatus("meeting-public-id", request, 10L);
    }

    @Test
    void deleteMeeting_passesOwnerUserIdToCommandService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());

        ResponseEntity<Void> response =
                meetingController.deleteMeeting(authentication, "meeting-public-id");

        verify(meetingCommandService).deleteMeeting("meeting-public-id", 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void createCompetition_passesOwnerUserIdToCompetitionCreateService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());
        when(competitionCreateService.createCompetition("meeting-public-id", 10L))
                .thenReturn(new MeetingCompetitionCreateResponse("competition-public-id"));

        ResponseEntity<ResponseDTO<MeetingCompetitionCreateResponse>> response =
                meetingController.createCompetition(authentication, "meeting-public-id");

        verify(competitionCreateService).createCompetition("meeting-public-id", 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("competition-public-id");
    }

    @Test
    void deleteCompetition_passesOwnerUserIdToCompetitionCreateService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(10L, null, List.of());

        ResponseEntity<Void> response =
                meetingController.deleteCompetition(authentication, "meeting-public-id");

        verify(competitionCreateService).deleteCompetition("meeting-public-id", 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
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

    private static MeetingDetailResponse detailResponse(String publicId, boolean ownedByCurrentUser) {
        return new MeetingDetailResponse(
                publicId,
                10L,
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
                false,
                List.of()
        );
    }
}
