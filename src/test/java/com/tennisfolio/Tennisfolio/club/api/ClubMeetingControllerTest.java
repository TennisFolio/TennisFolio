package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.service.ClubMeetingCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMeetingQueryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
        Long userId = 10L;
        when(clubMeetingQueryService.getClubMeetings("club-public-id", 10L))
                .thenReturn(List.of(summaryResponse()));

        ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> response =
                clubMeetingController.getClubMeetings(userId, "club-public-id");

        verify(clubMeetingQueryService).getClubMeetings("club-public-id", 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void createClubMeeting_passesCurrentUserToCommandService() {
        Long userId = 10L;
        MeetingCreateRequest request = createRequest();
        when(clubMeetingCommandService.createClubMeeting("club-public-id", request, 10L))
                .thenReturn(new MeetingCreateResponse("meeting-public-id"));

        ResponseEntity<ResponseDTO<MeetingCreateResponse>> response =
                clubMeetingController.createClubMeeting(userId, "club-public-id", request);

        verify(clubMeetingCommandService).createClubMeeting("club-public-id", request, 10L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void getClubMeeting_passesCurrentUserToQueryService() {
        Long userId = 20L;
        when(clubMeetingQueryService.getClubMeeting("club-public-id", "meeting-public-id", 20L))
                .thenReturn(detailResponse(false));

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                clubMeetingController.getClubMeeting(userId, "club-public-id", "meeting-public-id");

        verify(clubMeetingQueryService).getClubMeeting("club-public-id", "meeting-public-id", 20L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("meeting-public-id");
    }

    @Test
    void updateClubMeeting_passesCurrentUserToCommandService() {
        Long userId = 20L;
        MeetingUpdateRequest request = updateRequest();
        when(clubMeetingCommandService.updateClubMeeting("club-public-id", "meeting-public-id", request, 20L))
                .thenReturn(detailResponse(false));

        ResponseEntity<ResponseDTO<MeetingDetailResponse>> response =
                clubMeetingController.updateClubMeeting(
                        userId,
                        "club-public-id",
                        "meeting-public-id",
                        request
                );

        verify(clubMeetingCommandService).updateClubMeeting("club-public-id", "meeting-public-id", request, 20L);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void deleteClubMeeting_passesCurrentUserToCommandService() {
        Long userId = 20L;

        ResponseEntity<Void> response =
                clubMeetingController.deleteClubMeeting(userId, "club-public-id", "meeting-public-id");

        verify(clubMeetingCommandService).deleteClubMeeting("club-public-id", "meeting-public-id", 20L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void createClubMeetingCompetition_passesCurrentUserToCommandService() {
        Long userId = 20L;
        MeetingCompetitionCreateRequest request = new MeetingCompetitionCreateRequest(true);
        when(clubMeetingCommandService.createClubMeetingCompetition(
                "club-public-id",
                "meeting-public-id",
                request,
                20L
        )).thenReturn(new MeetingCompetitionCreateResponse("competition-public-id"));

        ResponseEntity<ResponseDTO<MeetingCompetitionCreateResponse>> response =
                clubMeetingController.createClubMeetingCompetition(
                        userId,
                        "club-public-id",
                        "meeting-public-id",
                        request
                );

        verify(clubMeetingCommandService).createClubMeetingCompetition(
                "club-public-id",
                "meeting-public-id",
                request,
                20L
        );
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getPublicId()).isEqualTo("competition-public-id");
    }

    @Test
    void createClubMeetingCompetition_usesDefaultRequestWhenBodyIsMissing() {
        Long userId = 20L;
        when(clubMeetingCommandService.createClubMeetingCompetition(
                eq("club-public-id"),
                eq("meeting-public-id"),
                argThat(request -> !request.isSameGenderDoublesOnly()),
                eq(20L)
        )).thenReturn(new MeetingCompetitionCreateResponse("competition-public-id"));

        clubMeetingController.createClubMeetingCompetition(
                userId,
                "club-public-id",
                "meeting-public-id",
                null
        );

        verify(clubMeetingCommandService).createClubMeetingCompetition(
                eq("club-public-id"),
                eq("meeting-public-id"),
                argThat(request -> !request.isSameGenderDoublesOnly()),
                eq(20L)
        );
    }

    @Test
    void deleteClubMeetingCompetition_passesCurrentUserToCommandService() {
        Long userId = 20L;

        ResponseEntity<Void> response =
                clubMeetingController.deleteClubMeetingCompetition(
                        userId,
                        "club-public-id",
                        "meeting-public-id"
                );

        verify(clubMeetingCommandService).deleteClubMeetingCompetition("club-public-id", "meeting-public-id", 20L);
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
