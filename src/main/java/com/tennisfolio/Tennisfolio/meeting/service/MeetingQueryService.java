package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingDetailResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingSummaryResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    public MeetingQueryService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository,
            CompetitionRepository competitionRepository,
            UserRepository userRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
        this.competitionRepository = competitionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getMeeting(String publicId, Long currentUserId) {
        Meeting meeting = findActiveMeeting(publicId);
        return MeetingDetailResponse.from(
                meeting,
                currentUserId,
                findCompetitionPublicId(meeting),
                findOwnerNickName(meeting),
                attendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting)
        );
    }

    private String findCompetitionPublicId(Meeting meeting) {
        if (!meeting.hasCompetition()) {
            return null;
        }

        return competitionRepository.findByIdAndDeletedAtIsNull(meeting.getCompetitionId())
                .map(competition -> competition.getPublicId())
                .orElse(null);
    }

    private String findOwnerNickName(Meeting meeting) {
        return userRepository.findByIdAndStatus(meeting.getOwnerUserId(), UserStatus.ACTIVE)
                .map(user -> user.getNickName())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getOwnedMeetings(Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByOwnerUserIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(ownerUserId)
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingSummaryResponse> getClubMeetings(Long clubId) {
        if (clubId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽 ID가 필요합니다.");
        }
        return meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(clubId)
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
                        AttendanceStatus.WAITING
                ),
                attendanceRepository.countByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                        meeting,
                        AttendanceStatus.NOT_ATTENDING
                )
        );
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
