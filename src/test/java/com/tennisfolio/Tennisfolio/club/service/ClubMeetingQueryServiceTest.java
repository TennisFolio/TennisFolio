package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
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
class ClubMeetingQueryServiceTest {

    @Mock
    ClubAccessService clubAccessService;

    @Mock
    MeetingQueryService meetingQueryService;

    @InjectMocks
    ClubMeetingQueryService service;

    @Test
    void getClubMeetings_checksMemberAndReturnsClubMeetingSummaries() {
        Club club = club(100L);
        when(clubAccessService.requireActiveMemberClub("club-public-id", 10L)).thenReturn(club);
        when(meetingQueryService.getClubMeetings(100L)).thenReturn(List.of(summaryResponse()));

        List<MeetingSummaryResponse> response = service.getClubMeetings("club-public-id", 10L);

        verify(clubAccessService).requireActiveMemberClub("club-public-id", 10L);
        verify(meetingQueryService).getClubMeetings(100L);
        assertThat(response).hasSize(1);
    }

    @Test
    void getClubMeeting_checksMemberFindsDomainAndMapsResponse() {
        Club club = club(100L);
        Meeting meeting = meeting();
        MeetingDetailResponse detail = detailResponse(false);
        when(clubAccessService.requireActiveMemberClub("club-public-id", 20L)).thenReturn(club);
        when(meetingQueryService.findActiveClubMeeting("meeting-public-id", 100L)).thenReturn(meeting);
        when(meetingQueryService.toDetailResponse(meeting, 20L)).thenReturn(detail);

        MeetingDetailResponse response = service.getClubMeeting("club-public-id", "meeting-public-id", 20L);

        verify(clubAccessService).requireActiveMemberClub("club-public-id", 20L);
        verify(meetingQueryService).findActiveClubMeeting("meeting-public-id", 100L);
        verify(meetingQueryService).toDetailResponse(meeting, 20L);
        assertThat(response.getPublicId()).isEqualTo("meeting-public-id");
    }

    private static Club club(Long id) {
        Club club = new Club("Morning Tennis", "Indoor club", 10L);
        ReflectionTestUtils.setField(club, "id", id);
        return club;
    }

    private static Meeting meeting() {
        Meeting meeting = new Meeting(
                10L,
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
        meeting.connectClub(100L);
        return meeting;
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
