package com.tennisfolio.Tennisfolio.meeting.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingAttendanceCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCommandService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingCompetitionCreateService;
import com.tennisfolio.Tennisfolio.meeting.service.MeetingQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal Long userId,
            @RequestBody MeetingCreateRequest request
    ) {
        MeetingCreateResponse response =
                meetingCommandService.createMeeting(request, userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/meetings")
    public ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> getPublicMeetings() {
        List<MeetingSummaryResponse> response = meetingQueryService.getPublicMeetings();
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/meetings/{publicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> getMeeting(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId
    ) {
        MeetingDetailResponse response =
                meetingQueryService.getMeeting(publicId, userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @GetMapping("/me/meetings")
    public ResponseEntity<ResponseDTO<List<MeetingSummaryResponse>>> getMyMeetings(
            @AuthenticationPrincipal Long userId
    ) {
        List<MeetingSummaryResponse> response =
                meetingQueryService.getOwnedMeetings(userId);
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/meetings/{publicId}")
    public ResponseEntity<ResponseDTO<MeetingDetailResponse>> updateMeeting(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId,
            @RequestBody MeetingUpdateRequest request
    ) {
        MeetingDetailResponse response = meetingCommandService.updateMeeting(
                publicId,
                request,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}")
    public ResponseEntity<Void> deleteMeeting(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId
    ) {
        meetingCommandService.deleteMeeting(publicId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meetings/{publicId}/participants")
    public ResponseEntity<ResponseDTO<MeetingAttendanceResponse>> addManagedParticipant(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId,
            @RequestBody ManagedMeetingParticipantCreateRequest request
    ) {
        MeetingAttendanceResponse response = attendanceCommandService.addManagedParticipant(
                publicId,
                request,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @PatchMapping("/meetings/{publicId}/participants/{attendanceId}")
    public ResponseEntity<ResponseDTO<MeetingAttendanceResponse>> updateManagedParticipant(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId,
            @PathVariable Long attendanceId,
            @RequestBody ManagedMeetingParticipantUpdateRequest request
    ) {
        MeetingAttendanceResponse response = attendanceCommandService.updateManagedParticipant(
                publicId,
                attendanceId,
                request,
                userId
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}/participants/{attendanceId}")
    public ResponseEntity<Void> deleteManagedParticipant(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId,
            @PathVariable Long attendanceId
    ) {
        attendanceCommandService.deleteAttendance(
                publicId,
                attendanceId,
                userId
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/meetings/{publicId}/competition")
    public ResponseEntity<ResponseDTO<MeetingCompetitionCreateResponse>> createCompetition(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId,
            @RequestBody(required = false) MeetingCompetitionCreateRequest request
    ) {
        MeetingCompetitionCreateResponse response = competitionCreateService.createCompetition(
                publicId,
                userId,
                request == null ? MeetingCompetitionCreateRequest.defaults() : request
        );
        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @DeleteMapping("/meetings/{publicId}/competition")
    public ResponseEntity<Void> deleteCompetition(
            @AuthenticationPrincipal Long userId,
            @PathVariable String publicId
    ) {
        competitionCreateService.deleteCompetition(publicId, userId);
        return ResponseEntity.noContent().build();
    }

}
