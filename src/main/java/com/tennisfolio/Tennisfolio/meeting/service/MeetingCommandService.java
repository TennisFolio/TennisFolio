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
        return createMeeting(request, ownerUserId, null);
    }

    @Transactional
    public MeetingCreateResponse createClubMeeting(MeetingCreateRequest request, Long ownerUserId, Long clubId) {
        if (clubId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽 ID가 필요합니다.");
        }
        return createMeeting(request, ownerUserId, clubId);
    }

    private MeetingCreateResponse createMeeting(MeetingCreateRequest request, Long ownerUserId, Long clubId) {
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
        Meeting meeting = new Meeting(
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
        );
        if (clubId != null) {
            meeting.connectClub(clubId);
        }
        meeting = meetingRepository.save(meeting);
        return new MeetingCreateResponse(meeting.getPublicId());
    }

    @Transactional
    public MeetingDetailResponse updateMeeting(String publicId, MeetingUpdateRequest request, Long ownerUserId) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        updateMeetingDetails(meeting, request);
        return MeetingDetailResponse.from(meeting, ownerUserId);
    }

    @Transactional
    public MeetingDetailResponse updateClubMeeting(
            String publicId,
            Long clubId,
            MeetingUpdateRequest request,
            Long currentUserId
    ) {
        Meeting meeting = findClubMeeting(publicId, clubId);
        updateMeetingDetails(meeting, request);
        return MeetingDetailResponse.from(meeting, currentUserId);
    }

    private void updateMeetingDetails(Meeting meeting, MeetingUpdateRequest request) {
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
        if (meeting.hasCompetition()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "대진표가 생성된 모임은 수정할 수 없습니다. 수정하려면 대진표를 먼저 삭제해 주세요."
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
    public MeetingDetailResponse updateClubMeetingStatus(
            String publicId,
            Long clubId,
            MeetingStatusUpdateRequest request,
            Long currentUserId
    ) {
        Meeting meeting = findClubMeeting(publicId, clubId);
        MeetingStatus status = parseStatus(request.getStatus());
        meeting.updateStatus(status);
        return MeetingDetailResponse.from(meeting, currentUserId);
    }

    @Transactional
    public void deleteMeeting(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        meeting.delete(LocalDateTime.now());
    }

    @Transactional
    public void deleteClubMeeting(String publicId, Long clubId) {
        Meeting meeting = findClubMeeting(publicId, clubId);
        meeting.delete(LocalDateTime.now());
    }

    private Meeting findOwnedMeeting(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private Meeting findClubMeeting(String publicId, Long clubId) {
        if (clubId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽 ID가 필요합니다.");
        }
        return meetingRepository.findByPublicIdAndClubIdAndDeletedAtIsNull(publicId, clubId)
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모임 이름을 입력해주세요.");
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료 시간은 시작 시간보다 늦어야 합니다.");
        }
        if (courtCount == null || courtCount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "코트 수는 1개 이상이어야 합니다.");
        }
        if (totalGames == null || totalGames < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "경기 수는 1개 이상이어야 합니다.");
        }
    }

    private void validateCapacityPolicy(
            Integer maxParticipants,
            Integer maxMaleParticipants,
            Integer maxFemaleParticipants
    ) {
        if (maxParticipants != null && (maxMaleParticipants != null || maxFemaleParticipants != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "전체 정원과 성별 정원은 함께 설정할 수 없습니다."
            );
        }
    }

    private MeetingStatus parseStatus(String status) {
        try {
            return MeetingStatus.valueOf(status);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모임 상태 값이 올바르지 않습니다.");
        }
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
