package com.tennisfolio.Tennisfolio.club.api;

import com.tennisfolio.Tennisfolio.club.service.ClubMeetingCommandService;
import com.tennisfolio.Tennisfolio.club.service.ClubMeetingQueryService;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clubs/{clubPublicId}/meetings")
public class ClubMeetingController {

    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubMeetingQueryService clubMeetingQueryService;

    public ClubMeetingController(
            ClubMeetingCommandService clubMeetingCommandService,
            ClubMeetingQueryService clubMeetingQueryService
    ) {
        this.clubMeetingCommandService = clubMeetingCommandService;
        this.clubMeetingQueryService = clubMeetingQueryService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> getClubMeetings(
            Authentication authentication,
            @PathVariable String clubPublicId
    ) {
        List<MeetingSummaryResponse> response = clubMeetingQueryService.getClubMeetings(
                clubPublicId,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<MeetingCreateResponse>> createClubMeeting(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @RequestBody MeetingCreateRequest request
    ) {
        MeetingCreateResponse response = clubMeetingCommandService.createClubMeeting(
                clubPublicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/{meetingPublicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> getClubMeeting(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId
    ) {
        MeetingDetailResponse response = clubMeetingQueryService.getClubMeeting(
                clubPublicId,
                meetingPublicId,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/{meetingPublicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> updateClubMeeting(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId,
            @RequestBody MeetingUpdateRequest request
    ) {
        MeetingDetailResponse response = clubMeetingCommandService.updateClubMeeting(
                clubPublicId,
                meetingPublicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/{meetingPublicId}/status")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> updateClubMeetingStatus(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId,
            @RequestBody MeetingStatusUpdateRequest request
    ) {
        MeetingDetailResponse response = clubMeetingCommandService.updateClubMeetingStatus(
                clubPublicId,
                meetingPublicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/{meetingPublicId}")
    public ResponseEntity<Void> deleteClubMeeting(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId
    ) {
        clubMeetingCommandService.deleteClubMeeting(
                clubPublicId,
                meetingPublicId,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{meetingPublicId}/competition")
    public ResponseEntity<ResponseDTO<MeetingCompetitionCreateResponse>> createClubMeetingCompetition(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId,
            @RequestBody(required = false) MeetingCompetitionCreateRequest request
    ) {
        MeetingCompetitionCreateResponse response = clubMeetingCommandService.createClubMeetingCompetition(
                clubPublicId,
                meetingPublicId,
                request == null ? MeetingCompetitionCreateRequest.defaults() : request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/{meetingPublicId}/competition")
    public ResponseEntity<Void> deleteClubMeetingCompetition(
            Authentication authentication,
            @PathVariable String clubPublicId,
            @PathVariable String meetingPublicId
    ) {
        clubMeetingCommandService.deleteClubMeetingCompetition(
                clubPublicId,
                meetingPublicId,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.noContent().build();
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
