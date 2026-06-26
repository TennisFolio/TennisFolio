package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCreationResult;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeetingCompetitionCreateService {

    private static final String COMPETITION_MODE = "FIXED_SCHEDULE";

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final CompetitionCommandService competitionCommandService;
    private final CompetitionRepository competitionRepository;

    public MeetingCompetitionCreateService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository,
            CompetitionCommandService competitionCommandService,
            CompetitionRepository competitionRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
        this.competitionCommandService = competitionCommandService;
        this.competitionRepository = competitionRepository;
    }

    @Transactional
    public MeetingCompetitionCreateResponse createCompetition(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeetingForUpdate(publicId, ownerUserId);
        if (meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting already has competition");
        }

        CompetitionCreateRequest request = toCompetitionCreateRequest(meeting, findAttendingParticipants(meeting));
        CompetitionCreationResult result = competitionCommandService.createCompetitionResult(request, ownerUserId);
        Competition competition = result.getCompetition();
        meeting.connectCompetition(competition.getId());
        return new MeetingCompetitionCreateResponse(competition.getPublicId());
    }

    @Transactional
    public void deleteCompetition(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeetingForUpdate(publicId, ownerUserId);
        if (!meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meeting has no competition");
        }

        Competition competition = competitionRepository
                .findByIdAndOwnerUserIdAndDeletedAtIsNull(meeting.getCompetitionId(), ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competition.delete(LocalDateTime.now());
        meeting.clearCompetition();
    }

    private Meeting findOwnedMeetingForUpdate(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private List<MeetingAttendance> findAttendingParticipants(Meeting meeting) {
        return attendanceRepository.findByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        );
    }

    private CompetitionCreateRequest toCompetitionCreateRequest(
            Meeting meeting,
            List<MeetingAttendance> attendances
    ) {
        List<String> malePlayerNames = playerNamesByGender(attendances, Gender.MALE);
        List<String> femalePlayerNames = playerNamesByGender(attendances, Gender.FEMALE);
        return new CompetitionCreateRequest(
                COMPETITION_MODE,
                meeting.getTitle(),
                malePlayerNames.size(),
                femalePlayerNames.size(),
                meeting.getCourtCount(),
                meeting.getTotalGames(),
                null,
                malePlayerNames,
                femalePlayerNames
        );
    }

    private List<String> playerNamesByGender(List<MeetingAttendance> attendances, Gender gender) {
        return attendances.stream()
                .filter(attendance -> attendance.getGender() == gender)
                .map(MeetingAttendance::getParticipantName)
                .toList();
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }
}
