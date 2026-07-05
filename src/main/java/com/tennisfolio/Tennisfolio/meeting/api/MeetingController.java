package com.tennisfolio.Tennisfolio.meeting.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCompetitionCreateService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
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
@RequestMapping("/api")
public class MeetingController {

    private final MeetingCommandService meetingCommandService;
    private final MeetingQueryService meetingQueryService;
    private final MeetingAttendanceCommandService attendanceCommandService;
    private final MeetingCompetitionCreateService competitionCreateService;

    public MeetingController(
            MeetingCommandService meetingCommandService,
            MeetingQueryService meetingQueryService,
            MeetingAttendanceCommandService attendanceCommandService,
            MeetingCompetitionCreateService competitionCreateService
    ) {
        this.meetingCommandService = meetingCommandService;
        this.meetingQueryService = meetingQueryService;
        this.attendanceCommandService = attendanceCommandService;
        this.competitionCreateService = competitionCreateService;
    }

    @PostMapping("/meetings")
    public ResponseEntity<ResponseDTO<MeetingCreateResponse>> createMeeting(
            Authentication authentication,
            @RequestBody MeetingCreateRequest request
    ) {
        MeetingCreateResponse response =
                meetingCommandService.createMeeting(request, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/meetings/{publicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> getMeeting(
            Authentication authentication,
            @PathVariable String publicId
    ) {
        MeetingDetailResponse response =
                meetingQueryService.getMeeting(publicId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/me/meetings")
    public ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> getMyMeetings(
            Authentication authentication
    ) {
        List<MeetingSummaryResponse> response =
                meetingQueryService.getOwnedMeetings(resolveAuthenticatedUserId(authentication));
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/meetings/{publicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> updateMeeting(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestBody MeetingUpdateRequest request
    ) {
        MeetingDetailResponse response = meetingCommandService.updateMeeting(
                publicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/meetings/{publicId}/status")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> updateMeetingStatus(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestBody MeetingStatusUpdateRequest request
    ) {
        MeetingDetailResponse response = meetingCommandService.updateStatus(
                publicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}")
    public ResponseEntity<Void> deleteMeeting(
            Authentication authentication,
            @PathVariable String publicId
    ) {
        meetingCommandService.deleteMeeting(publicId, resolveAuthenticatedUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meetings/{publicId}/attendances")
    public ResponseEntity<ResponseDTO<MeetingAttendanceResponse>> upsertAttendance(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestBody MeetingAttendanceUpsertRequest request
    ) {
        MeetingAttendanceResponse response = attendanceCommandService.upsertAttendance(
                publicId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/meetings/{publicId}/attendances/{attendanceId}")
    public ResponseEntity<ResponseDTO<MeetingAttendanceResponse>> updateAttendance(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long attendanceId,
            @RequestBody MeetingAttendanceUpsertRequest request
    ) {
        MeetingAttendanceResponse response = attendanceCommandService.updateAttendance(
                publicId,
                attendanceId,
                request,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}/attendances/{attendanceId}")
    public ResponseEntity<Void> deleteAttendance(
            Authentication authentication,
            @PathVariable String publicId,
            @PathVariable Long attendanceId
    ) {
        attendanceCommandService.deleteAttendance(
                publicId,
                attendanceId,
                resolveAuthenticatedUserId(authentication)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meetings/{publicId}/competition")
    public ResponseEntity<ResponseDTO<MeetingCompetitionCreateResponse>> createCompetition(
            Authentication authentication,
            @PathVariable String publicId,
            @RequestBody(required = false) MeetingCompetitionCreateRequest request
    ) {
        MeetingCompetitionCreateResponse response = competitionCreateService.createCompetition(
                publicId,
                resolveAuthenticatedUserId(authentication),
                request == null ? MeetingCompetitionCreateRequest.defaults() : request
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}/competition")
    public ResponseEntity<Void> deleteCompetition(
            Authentication authentication,
            @PathVariable String publicId
    ) {
        competitionCreateService.deleteCompetition(publicId, resolveAuthenticatedUserId(authentication));
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
