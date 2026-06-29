package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class MeetingAttendanceCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;

    public MeetingAttendanceCommandService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public MeetingAttendanceResponse upsertAttendance(String publicId, MeetingAttendanceUpsertRequest request) {
        Meeting meeting = findActiveMeetingForAttendanceUpdate(publicId);
        ensureAttendanceEditable(meeting);
        String participantName = requireParticipantName(request.getParticipantName());
        Gender gender = parseGender(request.getGender());
        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());

        MeetingAttendance attendance;
        if (request.getAttendanceId() == null) {
            rejectDuplicateName(meeting, participantName);
            ensureCapacityAvailable(meeting, null, gender, status);
            attendance = attendanceRepository.save(new MeetingAttendance(meeting, participantName, gender, status));
        } else {
            attendance = findAttendance(request.getAttendanceId(), meeting);
            rejectDuplicateNameExceptSelf(meeting, participantName, attendance.getId());
            ensureCapacityAvailable(meeting, attendance, gender, status);
            attendance.update(participantName, gender, status);
        }
        return MeetingAttendanceResponse.from(attendance);
    }

    @Transactional
    public MeetingAttendanceResponse updateAttendance(
            String publicId,
            Long attendanceId,
            MeetingAttendanceUpsertRequest request,
            Long ownerUserId
    ) {
        Meeting meeting = findOwnedMeetingForAttendanceUpdate(publicId, ownerUserId);

        ensureAttendanceEditable(meeting);

        MeetingAttendance attendance = findAttendance(attendanceId, meeting);

        String participantName = requireParticipantName(request.getParticipantName());

        Gender gender = parseGender(request.getGender());

        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());

        rejectDuplicateNameExceptSelf(meeting, participantName, attendance.getId());

        ensureCapacityAvailable(meeting, attendance, gender, status);

        attendance.update(participantName, gender, status);
        return MeetingAttendanceResponse.from(attendance);
    }

    @Transactional
    public void deleteAttendance(String publicId, Long attendanceId, Long ownerUserId) {
        Meeting meeting = findOwnedMeetingForAttendanceUpdate(publicId, ownerUserId);
        ensureAttendanceEditable(meeting);
        MeetingAttendance attendance = findAttendance(attendanceId, meeting);
        attendance.delete(LocalDateTime.now());
    }

    private Meeting findActiveMeetingForAttendanceUpdate(String publicId) {
        return meetingRepository.findByPublicIdAndDeletedAtIsNullForUpdate(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private Meeting findOwnedMeetingForAttendanceUpdate(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private MeetingAttendance findAttendance(Long attendanceId, Meeting meeting) {
        return attendanceRepository.findByIdAndMeetingAndDeletedAtIsNull(attendanceId, meeting)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private void ensureAttendanceEditable(Meeting meeting) {
        if (meeting.getStatus() != MeetingStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "참석 체크가 마감되었습니다.");
        }
        if (meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 경기표가 생성된 모임입니다.");
        }
    }

    private String requireParticipantName(String participantName) {
        if (participantName == null || participantName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해주세요.");
        }
        return participantName.trim();
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "성별 값이 올바르지 않습니다.");
        }
    }

    private AttendanceStatus parseAttendanceStatus(String status) {
        try {
            return AttendanceStatus.valueOf(status);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참석 상태 값이 올바르지 않습니다.");
        }
    }

    private void rejectDuplicateName(Meeting meeting, String participantName) {
        if (attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNull(meeting, participantName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 이름으로 참석 응답이 등록되었습니다.");
        }
    }

    private void rejectDuplicateNameExceptSelf(Meeting meeting, String participantName, Long attendanceId) {
        if (attendanceRepository.existsByMeetingAndParticipantNameAndDeletedAtIsNullAndIdNot(
                meeting,
                participantName,
                attendanceId
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 이름으로 참석 응답이 등록되었습니다.");
        }
    }

    private void ensureCapacityAvailable(
            Meeting meeting,
            MeetingAttendance currentAttendance,
            Gender requestedGender,
            AttendanceStatus requestedStatus
    ) {
        if (requestedStatus != AttendanceStatus.ATTENDING) {
            return;
        }
        if (meeting.getMaxParticipants() != null) {
            ensureTotalCapacityAvailable(meeting, currentAttendance);
            return;
        }
        ensureGenderCapacityAvailable(meeting, currentAttendance, requestedGender);
    }

    private void ensureTotalCapacityAvailable(Meeting meeting, MeetingAttendance currentAttendance) {
        long otherAttendingCount = countTotalAttendingExcludingCurrent(meeting, currentAttendance);
        if (otherAttendingCount >= meeting.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "참석 가능 인원이 모두 찼습니다.");
        }
    }

    private long countTotalAttendingExcludingCurrent(Meeting meeting, MeetingAttendance currentAttendance) {
        long totalAttending = attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        );
        if (currentAttendance != null
                && currentAttendance.getAttendanceStatus() == AttendanceStatus.ATTENDING) {
            totalAttending--;
        }
        return totalAttending;
    }

    private void ensureGenderCapacityAvailable(
            Meeting meeting,
            MeetingAttendance currentAttendance,
            Gender requestedGender
    ) {
        Integer genderCapacity = genderCapacityOf(meeting, requestedGender);
        if (genderCapacity == null) {
            return;
        }
        long otherGenderAttendingCount =
                countGenderAttendingExcludingCurrent(meeting, currentAttendance, requestedGender);
        if (otherGenderAttendingCount >= genderCapacity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 성별의 참석 가능 인원이 모두 찼습니다.");
        }
    }

    private Integer genderCapacityOf(Meeting meeting, Gender requestedGender) {
        return requestedGender == Gender.MALE
                ? meeting.getMaxMaleParticipants()
                : meeting.getMaxFemaleParticipants();
    }

    private long countGenderAttendingExcludingCurrent(
            Meeting meeting,
            MeetingAttendance currentAttendance,
            Gender requestedGender
    ) {
        long genderAttending = attendanceRepository.countByMeetingAndGenderAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                requestedGender,
                AttendanceStatus.ATTENDING
        );
        if (currentAttendance != null
                && currentAttendance.getAttendanceStatus() == AttendanceStatus.ATTENDING
                && currentAttendance.getGender() == requestedGender) {
            genderAttending--;
        }
        return genderAttending;
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
