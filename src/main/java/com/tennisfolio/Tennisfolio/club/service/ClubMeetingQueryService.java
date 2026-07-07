package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClubMeetingQueryService {

    private final ClubAccessService clubAccessService;
    private final MeetingQueryService meetingQueryService;

    public ClubMeetingQueryService(
            ClubAccessService clubAccessService,
            MeetingQueryService meetingQueryService
    ) {
        this.clubAccessService = clubAccessService;
        this.meetingQueryService = meetingQueryService;
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getClubMeetings(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireActiveMemberClub(clubPublicId, currentUserId);
        return meetingQueryService.getClubMeetings(club.getId());
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getClubMeeting(
            String clubPublicId,
            String meetingPublicId,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireActiveMemberClub(clubPublicId, currentUserId);
        Meeting meeting = meetingQueryService.findActiveClubMeeting(meetingPublicId, club.getId());
        return meetingQueryService.toDetailResponse(meeting, currentUserId);
    }
}
