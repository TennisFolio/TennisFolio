package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.dto.CompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCommandService;
import com.tennisfolio.Tennisfolio.matching.domain.GamePlayer;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleGenerationOptions;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleGenerationRequest;
import com.tennisfolio.Tennisfolio.matching.domain.ScheduleParticipant;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.matching.service.CompetitionCreationResult;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingCompetitionCreateResponse;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MeetingCompetitionCreateService {

    private static final String COMPETITION_MODE = "FIXED_SCHEDULE";

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final CompetitionCommandService competitionCommandService;
    private final CompetitionRepository competitionRepository;
    private final ClubSkillTierRepository clubSkillTierRepository;

    @Autowired
    public MeetingCompetitionCreateService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository,
            CompetitionCommandService competitionCommandService,
            CompetitionRepository competitionRepository,
            ClubSkillTierRepository clubSkillTierRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
        this.competitionCommandService = competitionCommandService;
        this.competitionRepository = competitionRepository;
        this.clubSkillTierRepository = clubSkillTierRepository;
    }

    MeetingCompetitionCreateService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository,
            CompetitionCommandService competitionCommandService,
            CompetitionRepository competitionRepository
    ) {
        this(meetingRepository, attendanceRepository, competitionCommandService, competitionRepository, null);
    }

    @Transactional
    public MeetingCompetitionCreateResponse createCompetition(String publicId, Long ownerUserId) {
        return createCompetition(publicId, ownerUserId, MeetingCompetitionCreateRequest.defaults());
    }

    @Transactional
    public MeetingCompetitionCreateResponse createCompetition(
            String publicId,
            Long ownerUserId,
            MeetingCompetitionCreateRequest request
    ) {
        Meeting meeting = findOwnedMeetingForUpdate(publicId, ownerUserId);
        return createCompetitionForMeeting(meeting, ownerUserId, request);
    }

    @Transactional
    public MeetingCompetitionCreateResponse createClubMeetingCompetition(
            String publicId,
            Long clubId,
            Long currentUserId,
            MeetingCompetitionCreateRequest request
    ) {
        requireAuthenticated(currentUserId);
        Meeting meeting = findClubMeetingForUpdate(publicId, clubId);
        return createCompetitionForMeeting(meeting, currentUserId, request);
    }

    private MeetingCompetitionCreateResponse createCompetitionForMeeting(
            Meeting meeting,
            Long creatorUserId,
            MeetingCompetitionCreateRequest request
    ) {
        if (meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 경기표가 생성된 모임입니다.");
        }

        List<MeetingAttendance> attendingParticipants = findAttendingParticipants(meeting);
        validateEnoughAttendingParticipants(meeting, attendingParticipants);

        CompetitionCreateRequest competitionRequest = toCompetitionCreateRequest(
                meeting,
                attendingParticipants,
                request.isSameGenderDoublesOnly()
        );
        CompetitionCreationResult result = request.isSkillBalancedSchedule()
                ? competitionCommandService.createCompetitionResult(
                        competitionRequest,
                        creatorUserId,
                        createSkillBalancedScheduleRequest(meeting, attendingParticipants, request)
                )
                : competitionCommandService.createCompetitionResult(competitionRequest, creatorUserId);
        Competition competition = result.getCompetition();
        meeting.connectCompetition(competition.getId());
        return new MeetingCompetitionCreateResponse(competition.getPublicId());
    }

    @Transactional
    public void deleteCompetition(String publicId, Long ownerUserId) {
        Meeting meeting = findOwnedMeetingForUpdate(publicId, ownerUserId);
        if (!meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "삭제할 경기표가 없습니다.");
        }

        Competition competition = competitionRepository
                .findByIdAndOwnerUserIdAndDeletedAtIsNull(meeting.getCompetitionId(), ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competition.delete(LocalDateTime.now());
        meeting.clearCompetition();
    }

    @Transactional
    public void deleteClubMeetingCompetition(String publicId, Long clubId) {
        Meeting meeting = findClubMeetingForUpdate(publicId, clubId);
        if (!meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "삭제할 경기표가 없습니다.");
        }

        Competition competition = competitionRepository
                .findByIdAndDeletedAtIsNull(meeting.getCompetitionId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        competition.delete(LocalDateTime.now());
        meeting.clearCompetition();
    }

    private Meeting findOwnedMeetingForUpdate(String publicId, Long ownerUserId) {
        requireAuthenticated(ownerUserId);
        return meetingRepository.findByPublicIdAndOwnerUserIdAndDeletedAtIsNullForUpdate(publicId, ownerUserId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private Meeting findClubMeetingForUpdate(String publicId, Long clubId) {
        return meetingRepository.findByPublicIdAndClubIdAndDeletedAtIsNullForUpdate(publicId, clubId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private List<MeetingAttendance> findAttendingParticipants(Meeting meeting) {
        return attendanceRepository.findByMeetingAndAttendanceStatusAndDeletedAtIsNull(
                meeting,
                AttendanceStatus.ATTENDING
        );
    }

    private void validateEnoughAttendingParticipants(Meeting meeting, List<MeetingAttendance> attendances) {
        if (attendances.size() < meeting.getCourtCount() * 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "코트 수에 비해 참석자가 적습니다. 참석자를 늘리거나 코트 수를 줄여주세요."
            );
        }
    }

    private CompetitionCreateRequest toCompetitionCreateRequest(
            Meeting meeting,
            List<MeetingAttendance> attendances,
            boolean sameGenderDoublesOnly
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
                femalePlayerNames,
                sameGenderDoublesOnly
        );
    }

    private List<String> playerNamesByGender(List<MeetingAttendance> attendances, Gender gender) {
        return attendances.stream()
                .filter(attendance -> attendance.getGender() == gender)
                .map(MeetingAttendance::getParticipantName)
                .toList();
    }

    private ScheduleGenerationRequest createSkillBalancedScheduleRequest(
            Meeting meeting,
            List<MeetingAttendance> attendances,
            MeetingCompetitionCreateRequest request
    ) {
        if (meeting.getClubId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "실력 기반 대진은 클럽 모임에서만 사용할 수 있습니다.");
        }

        Map<Long, ClubSkillTier> tiersById = findClubSkillTiers(meeting.getClubId(), attendances);
        validateSkillTiers(attendances, tiersById);

        List<ScheduleParticipant> participants = toScheduleParticipants(attendances, tiersById);
        return new ScheduleGenerationRequest(
                participants,
                meeting.getCourtCount(),
                meeting.getTotalGames(),
                ThreadLocalRandom.current().nextLong(1, 10000),
                new ScheduleGenerationOptions(request.isSameGenderDoublesOnly(), true)
        );
    }

    private Map<Long, ClubSkillTier> findClubSkillTiers(Long clubId, List<MeetingAttendance> attendances) {
        List<Long> tierIds = attendances.stream()
                .map(MeetingAttendance::getClubSkillTierId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (tierIds.isEmpty()) {
            return Map.of();
        }
        return clubSkillTierRepository.findByClubIdAndIdIn(clubId, tierIds).stream()
                .collect(Collectors.toMap(ClubSkillTier::getId, Function.identity()));
    }

    private List<ScheduleParticipant> toScheduleParticipants(
            List<MeetingAttendance> attendances,
            Map<Long, ClubSkillTier> tiersById
    ) {
        List<ScheduleParticipant> participants = new ArrayList<>();
        int maleIndex = 1;
        int femaleIndex = 1;
        for (MeetingAttendance attendance : attendances) {
            boolean male = attendance.getGender() == Gender.MALE;
            String playerId = male ? "M" + maleIndex++ : "F" + femaleIndex++;
            participants.add(new ScheduleParticipant(
                    playerId,
                    toGamePlayerGender(attendance.getGender()),
                    tiersById.get(attendance.getClubSkillTierId()).getLevel()
            ));
        }
        return participants;
    }

    private void validateSkillTiers(List<MeetingAttendance> attendances, Map<Long, ClubSkillTier> tiersById) {
        List<String> missingParticipantNames = attendances.stream()
                .filter(attendance -> attendance.getClubSkillTierId() == null
                        || !tiersById.containsKey(attendance.getClubSkillTierId()))
                .map(MeetingAttendance::getParticipantName)
                .toList();
        if (!missingParticipantNames.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "실력 등급이 설정되지 않았거나 이 클럽에 속하지 않은 참석자: " + String.join(", ", missingParticipantNames)
            );
        }
    }

    private GamePlayer.Gender toGamePlayerGender(Gender gender) {
        return gender == Gender.MALE ? GamePlayer.Gender.MALE : GamePlayer.Gender.FEMALE;
    }

    private void requireAuthenticated(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
