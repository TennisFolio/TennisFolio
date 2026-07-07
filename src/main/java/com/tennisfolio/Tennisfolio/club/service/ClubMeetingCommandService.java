package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubMeetingCommandService {

    private final ClubAccessService clubAccessService;
    private final MeetingCommandService meetingCommandService;

    public ClubMeetingCommandService(
            ClubAccessService clubAccessService,
            MeetingCommandService meetingCommandService
    ) {
        this.clubAccessService = clubAccessService;
        this.meetingCommandService = meetingCommandService;
    }

    @Transactional
    public MeetingCreateResponse createClubMeeting(
            String clubPublicId,
            MeetingCreateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        return meetingCommandService.createClubMeeting(request, currentUserId, club.getId());
    }

    @Transactional
    public MeetingDetailResponse updateClubMeeting(
            String clubPublicId,
            String meetingPublicId,
            MeetingUpdateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        return meetingCommandService.updateClubMeeting(meetingPublicId, club.getId(), request, currentUserId);
    }

    @Transactional
    public MeetingDetailResponse updateClubMeetingStatus(
            String clubPublicId,
            String meetingPublicId,
            MeetingStatusUpdateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        return meetingCommandService.updateClubMeetingStatus(meetingPublicId, club.getId(), request, currentUserId);
    }

    @Transactional
    public void deleteClubMeeting(String clubPublicId, String meetingPublicId, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        meetingCommandService.deleteClubMeeting(meetingPublicId, club.getId());
    }
}
