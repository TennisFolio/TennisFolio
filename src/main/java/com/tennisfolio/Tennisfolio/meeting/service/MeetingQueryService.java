package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;

    public MeetingQueryService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeeting(String publicId, Long currentUserId) {
        Meeting meeting = findActiveMeeting(publicId);
        return MeetingDetailResponse.from(
                meeting,
                currentUserId,
                attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting)
        );
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getManagedMeeting(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeeting(publicId, ownerUserId);
        return MeetingDetailResponse.from(
                meeting,
                ownerUserId,
                attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting)
        );
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getOwnedMeetings(Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(ownerUserId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private MeetingSummaryResponse toSummaryResponse(Meeting meeting) {
        return MeetingSummaryResponse.from(
                meeting,
                attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                        meeting,
                        AttendanceStatus.ATTENDING
                ),
                attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                        meeting,
                        AttendanceStatus.MAYBE
                ),
                attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                        meeting,
                        AttendanceStatus.NOT_ATTENDING
                )
        );
    }

    private Meeting findOwnedMeeting(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNull(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private Meeting findActiveMeeting(String publicId) {
        return meetingRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
