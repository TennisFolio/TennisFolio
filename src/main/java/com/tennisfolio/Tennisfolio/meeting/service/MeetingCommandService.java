package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingStatusUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class MeetingCommandService {

    private final MeetingRepository meetingRepository;

    public MeetingCommandService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public MeetingCreateResponse createMeeting(MeetingCreateRequest request, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        validateRequiredDetails(
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.getCourtCount(),
                request.getTotalGames()
        );
        validateCapacityPolicy(
                request.getMaxParticipants(),
                request.getMaxMaleParticipants(),
                request.getMaxFemaleParticipants()
        );
        Meeting meeting = meetingRepository.save(new Meeting(
                ownerUserId,
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.getNote(),
                request.getMaxParticipants(),
                request.getMaxMaleParticipants(),
                request.getMaxFemaleParticipants(),
                request.getCourtCount(),
                request.getTotalGames()
        ));
        return new MeetingCreateResponse(meeting.getPublicId());
    }

    @Transactional
    public MeetingDetailResponse updateMeeting(String publicId, MeetingUpdateRequest request, Long ownerUserId) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        validateRequiredDetails(
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.getCourtCount(),
                request.getTotalGames()
        );
        validateCapacityPolicy(
                request.getMaxParticipants(),
                request.getMaxMaleParticipants(),
                request.getMaxFemaleParticipants()
        );
        if (meeting.hasCompetition() && scheduleConditionChanged(meeting, request)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot change courtCount or totalGames after competition creation"
            );
        }
        meeting.updateDetails(
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.getNote(),
                request.getMaxParticipants(),
                request.getMaxMaleParticipants(),
                request.getMaxFemaleParticipants(),
                request.getCourtCount(),
                request.getTotalGames()
        );
        return MeetingDetailResponse.from(meeting, ownerUserId);
    }

    @Transactional
    public MeetingDetailResponse updateStatus(
            String publicId,
            MeetingStatusUpdateRequest request,
            Long ownerUserId
    ) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        MeetingStatus status = parseStatus(request.getStatus());
        meeting.updateStatus(status);
        return MeetingDetailResponse.from(meeting, ownerUserId);
    }

    @Transactional
    public void deleteMeeting(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        meeting.delete(LocalDateTime.now());
    }

    private Meeting findOwnedMeeting(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private void validateRequiredDetails(
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer courtCount,
            Integer totalGames
    ) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endAt must be after startAt");
        }
        if (courtCount == null || courtCount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courtCount must be positive");
        }
        if (totalGames == null || totalGames < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalGames must be positive");
        }
    }

    private boolean scheduleConditionChanged(Meeting meeting, MeetingUpdateRequest request) {
        return !meeting.getCourtCount().equals(request.getCourtCount())
                || !meeting.getTotalGames().equals(request.getTotalGames());
    }

    private void validateCapacityPolicy(
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants
    ) {
        if (maxParticipants != null && (maxMaleParticipants != null || maxFemaleParticipants != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use either maxParticipants or gender capacity, not both"
            );
        }
    }

    private MeetingStatus parseStatus(String status) {
        try {
            return MeetingStatus.valueOf(status);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid meeting status");
        }
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }
}
