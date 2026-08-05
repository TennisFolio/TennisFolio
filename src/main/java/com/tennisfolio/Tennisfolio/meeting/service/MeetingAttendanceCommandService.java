package com.tennisfolio.Tennisfolio.meeting.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.AttendanceStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingStatus;
import com.tennisfolio.Tennisfolio.meeting.domain.MeetingParticipantType;
import com.tennisfolio.Tennisfolio.meeting.domain.ParticipantResolution;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceResponse;
import com.tennisfolio.Tennisfolio.meeting.dto.MeetingAttendanceUpsertRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantCreateRequest;
import com.tennisfolio.Tennisfolio.meeting.dto.ManagedMeetingParticipantUpdateRequest;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.entity.MeetingAttendance;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MeetingAttendanceCommandService {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubSkillTierRepository clubSkillTierRepository;

    public MeetingAttendanceCommandService(
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository attendanceRepository,
            UserRepository userRepository,
            ClubRepository clubRepository,
            ClubMemberRepository clubMemberRepository,
            ClubSkillTierRepository clubSkillTierRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.clubSkillTierRepository = clubSkillTierRepository;
    }

    @Transactional
    public MeetingAttendanceResponse upsertAttendance(
            String publicId,
            MeetingAttendanceUpsertRequest request,
            Long currentUserId
    ) {
        Meeting meeting = findActiveMeetingForAttendanceUpdate(publicId);
        ensureAttendanceEditable(meeting);
        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());
        Optional<MeetingAttendance> currentUserAttendance = findCurrentUserAttendance(meeting, currentUserId);
        if (currentUserAttendance.isPresent()) {
            MeetingAttendance attendance = currentUserAttendance.get();
            ensureRequestedAttendanceIsOwn(request.getAttendanceId(), attendance.getId());
            ensureCapacityAvailable(meeting, attendance, attendance.getGender(), status);
            attendance.update(
                    attendance.getParticipantName(),
                    attendance.getGender(),
                    status,
                    attendance.getParticipantType(),
                    attendance.getClubMemberId()
            );
            return MeetingAttendanceResponse.from(attendance);
        }
        rejectAuthenticatedUserUpdatingExistingAttendance(request.getAttendanceId(), currentUserId);

        ParticipantResolution participant = resolveParticipant(
                meeting,
                request.getParticipantName(),
                request.getGender(),
                currentUserId
        );
        ensureOwnerNameAvailableOnlyToOwner(meeting, participant.name(), currentUserId);

        MeetingAttendance attendance;
        if (request.getAttendanceId() == null) {
            rejectDuplicateName(meeting, participant.name());
            ensureCapacityAvailable(meeting, null, participant.gender(), status);
            MeetingAttendance newAttendance = new MeetingAttendance(meeting, participant.name(), participant.gender(), status);
            newAttendance.assignParticipant(participant.type(), participant.clubMemberId());
            applyParticipantSkillTier(newAttendance, participant, null);
            newAttendance.assignUser(currentUserId);
            attendance = attendanceRepository.save(newAttendance);
        } else {
            attendance = findAttendance(request.getAttendanceId(), meeting);
            rejectDuplicateNameExceptSelf(meeting, participant.name(), attendance.getId());
            ensureCapacityAvailable(meeting, attendance, participant.gender(), status);
            attendance.update(
                    participant.name(),
                    participant.gender(),
                    status,
                    participant.type(),
                    participant.clubMemberId()
            );
            applyParticipantSkillTier(attendance, participant, null);
        }
        return MeetingAttendanceResponse.from(attendance, skillTierNames(participant, null));
    }

    @Transactional
    public MeetingAttendanceResponse addManagedParticipant(
            String publicId,
            ManagedMeetingParticipantCreateRequest request,
            Long currentUserId
    ) {
        Meeting meeting = findActiveMeetingForAttendanceUpdate(publicId);
        ensureAttendanceEditable(meeting);
        ensureManagerCanAddParticipant(meeting, currentUserId);

        ParticipantResolution participant = resolveManagedParticipant(meeting, request);
        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());
        ensureGuestNameDoesNotMatchActiveClubMember(meeting, participant);
        ClubSkillTier guestSkillTier = resolveManagedGuestSkillTier(meeting, participant, request.getClubSkillTierId());
        Optional<MeetingAttendance> guestToPromote = findGuestToPromote(meeting, participant);
        if (guestToPromote.isPresent()) {
            MeetingAttendance attendance = guestToPromote.get();
            attendance.assignParticipant(MeetingParticipantType.CLUB_MEMBER, participant.clubMemberId());
            applyParticipantSkillTier(attendance, participant, guestSkillTier);
            return MeetingAttendanceResponse.from(attendance, skillTierNames(participant, guestSkillTier));
        }

        rejectDuplicateName(meeting, participant.name());
        ensureCapacityAvailable(meeting, null, participant.gender(), status);

        MeetingAttendance attendance = new MeetingAttendance(meeting, participant.name(), participant.gender(), status);
        attendance.assignParticipant(participant.type(), participant.clubMemberId());
        applyParticipantSkillTier(attendance, participant, guestSkillTier);
        MeetingAttendance savedAttendance = attendanceRepository.save(attendance);
        return MeetingAttendanceResponse.from(savedAttendance, skillTierNames(participant, guestSkillTier));
    }

    @Transactional
    public MeetingAttendanceResponse updateManagedParticipant(
            String publicId,
            Long attendanceId,
            ManagedMeetingParticipantUpdateRequest request,
            Long currentUserId
    ) {
        Meeting meeting = findActiveMeetingForAttendanceUpdate(publicId);
        ensureAttendanceEditable(meeting);
        ensureManagerCanAddParticipant(meeting, currentUserId);

        MeetingAttendance attendance = findAttendance(attendanceId, meeting);
        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());
        if (attendance.getParticipantType() == MeetingParticipantType.CLUB_MEMBER) {
            rejectClubMemberUpdate(request);
            ensureCapacityAvailable(meeting, attendance, attendance.getGender(), status);
            attendance.update(
                    attendance.getParticipantName(),
                    attendance.getGender(),
                    status,
                    attendance.getParticipantType(),
                    attendance.getClubMemberId()
            );
            return MeetingAttendanceResponse.from(attendance);
        }

        String participantName = requireParticipantName(request.getParticipantName());
        Gender gender = parseGender(request.getGender());
        ensureGuestNameDoesNotMatchActiveClubMember(meeting, ParticipantResolution.guest(participantName, gender));
        rejectDuplicateNameExceptSelf(meeting, participantName, attendance.getId());
        ensureCapacityAvailable(meeting, attendance, gender, status);
        ClubSkillTier guestSkillTier = resolveManagedGuestSkillTier(
                meeting,
                ParticipantResolution.guest(participantName, gender),
                request.getClubSkillTierId()
        );
        attendance.update(participantName, gender, status, MeetingParticipantType.GUEST, null);
        if (guestSkillTier == null) {
            attendance.clearClubSkillTier();
        } else {
            attendance.assignClubSkillTier(guestSkillTier.getId());
        }
        return MeetingAttendanceResponse.from(
                attendance,
                guestSkillTier == null ? java.util.Map.of() : java.util.Map.of(
                        guestSkillTier.getId(),
                        guestSkillTier.getName()
                )
        );
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

        ensureOwnerNameAvailableOnlyToOwner(meeting, participantName, ownerUserId);

        Gender gender = parseGender(request.getGender());

        ensureAccountLinkedIdentityUnchanged(attendance, participantName, gender);

        AttendanceStatus status = parseAttendanceStatus(request.getAttendanceStatus());

        rejectDuplicateNameExceptSelf(meeting, participantName, attendance.getId());

        ensureCapacityAvailable(meeting, attendance, gender, status);

        attendance.update(participantName, gender, status);
        return MeetingAttendanceResponse.from(attendance);
    }

    @Transactional
    public void deleteAttendance(String publicId, Long attendanceId, Long currentUserId) {
        Meeting meeting = findActiveMeetingForAttendanceUpdate(publicId);
        ensureAttendanceEditable(meeting);
        ensureManagerCanAddParticipant(meeting, currentUserId);
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

    private Optional<MeetingAttendance> findCurrentUserAttendance(Meeting meeting, Long currentUserId) {
        if (currentUserId == null) {
            return Optional.empty();
        }
        return attendanceRepository.findByMeetingAndUserIdAndDeletedAtIsNull(meeting, currentUserId);
    }

    private void ensureRequestedAttendanceIsOwn(Long requestedAttendanceId, Long ownAttendanceId) {
        if (requestedAttendanceId != null && !requestedAttendanceId.equals(ownAttendanceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인의 참석 정보만 수정할 수 있습니다.");
        }
    }

    private void rejectAuthenticatedUserUpdatingExistingAttendance(Long requestedAttendanceId, Long currentUserId) {
        if (currentUserId != null && requestedAttendanceId != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인의 참석 정보만 수정할 수 있습니다.");
        }
    }

    private void ensureAccountLinkedIdentityUnchanged(
            MeetingAttendance attendance,
            String participantName,
            Gender gender
    ) {
        if (attendance.getUserId() != null
                && (!attendance.getParticipantName().equals(participantName)
                || attendance.getGender() != gender)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "계정 연결 참석자의 이름과 성별은 수정할 수 없습니다.");
        }
    }

    private void ensureAttendanceEditable(Meeting meeting) {
        if (meeting.getStatus() != MeetingStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "참석 체크가 마감되었습니다.");
        }
        if (meeting.hasCompetition()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 경기표가 생성된 모임입니다.");
        }
    }

    private void ensureManagerCanAddParticipant(Meeting meeting, Long currentUserId) {
        requireAuthenticated(currentUserId);
        if (!meeting.isClubMeeting()) {
            if (!meeting.isOwnedBy(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "모임장만 참가자를 추가할 수 있습니다.");
            }
            return;
        }

        Club club = findActiveClub(meeting.getClubId());
        ClubMember member = clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 관리자만 참가자를 추가할 수 있습니다."));
        if (member.getRole() != ClubMemberRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 관리자만 참가자를 추가할 수 있습니다.");
        }
    }

    private void rejectClubMemberUpdate(ManagedMeetingParticipantUpdateRequest request) {
        if (request.getParticipantName() != null
                || request.getGender() != null
                || request.getClubSkillTierId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽원은 참가 상태만 수정할 수 있습니다.");
        }
    }

    private void ensureGuestNameDoesNotMatchActiveClubMember(
            Meeting meeting,
            ParticipantResolution participant
    ) {
        if (!meeting.isClubMeeting() || participant.type() != MeetingParticipantType.GUEST) {
            return;
        }

        Club club = findActiveClub(meeting.getClubId());
        if (clubMemberRepository.existsByClubAndNameAndActiveTrue(club, participant.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "클럽원과 같은 이름의 게스트는 등록할 수 없습니다. 동명이인 게스트는 이름을 구분해서 입력해주세요."
            );
        }
    }

    private String requireParticipantName(String participantName) {
        if (participantName == null || participantName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해주세요.");
        }
        return participantName.trim();
    }

    private void ensureOwnerNameAvailableOnlyToOwner(
            Meeting meeting,
            String participantName,
            Long currentUserId
    ) {
        String ownerNickName = userRepository.findByIdAndStatus(meeting.getOwnerUserId(), UserStatus.ACTIVE)
                .map(user -> user.getNickName())
                .orElse(null);

        if (ownerNickName == null || !ownerNickName.equals(participantName)) {
            return;
        }

        if (!meeting.isOwnedBy(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "모임장은 참석자로 선택할 수 없습니다.");
        }
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
            return AttendanceStatus.fromValue(status);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "참석 상태 값이 올바르지 않습니다.");
        }
    }

    private ParticipantResolution resolveParticipant(
            Meeting meeting,
            String participantName,
            String gender,
            Long currentUserId
    ) {
        if (currentUserId != null) {
            return resolveAuthenticatedParticipant(meeting, currentUserId);
        }

        String requestedName = requireParticipantName(participantName);
        Gender requestedGender = parseGender(gender);
        if (!meeting.isClubMeeting()) {
            return ParticipantResolution.guest(requestedName, requestedGender);
        }

        Club club = clubRepository.findByIdAndDeletedAtIsNull(meeting.getClubId())
                .orElse(null);
        if (club == null) {
            return ParticipantResolution.guest(requestedName, requestedGender);
        }

        List<ClubMember> exactMatches =
                clubMemberRepository.findByClubAndNameAndGenderAndActiveTrueOrderByIdAsc(
                        club,
                        requestedName,
                        requestedGender
                );
        if (exactMatches.size() == 1) {
            return clubMemberResolution(exactMatches.get(0));
        }

        return ParticipantResolution.guest(requestedName, requestedGender);
    }

    private ParticipantResolution resolveManagedParticipant(
            Meeting meeting,
            ManagedMeetingParticipantCreateRequest request
    ) {
        if (request.getClubMemberId() == null) {
            return ParticipantResolution.guest(
                    requireParticipantName(request.getParticipantName()),
                    parseGender(request.getGender())
            );
        }
        if (!meeting.isClubMeeting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개인 모임에는 클럽 멤버를 추가할 수 없습니다.");
        }

        Club club = findActiveClub(meeting.getClubId());
        ClubMember member = clubMemberRepository.findByClubAndIdAndActiveTrue(club, request.getClubMemberId())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        return clubMemberResolution(member);
    }

    private Club findActiveClub(Long clubId) {
        return clubRepository.findByIdAndDeletedAtIsNull(clubId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private ParticipantResolution resolveAuthenticatedParticipant(Meeting meeting, Long currentUserId) {
        if (meeting.isClubMeeting()) {
            Club club = clubRepository.findByIdAndDeletedAtIsNull(meeting.getClubId()).orElse(null);
            if (club != null) {
                Optional<ClubMember> currentMember =
                        clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, currentUserId);
                if (currentMember.isPresent()) {
                    return clubMemberResolution(currentMember.get());
                }
            }
        }

        User user = userRepository.findByIdAndStatus(currentUserId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        if (user.getNickName() == null || user.getNickName().isBlank() || user.getGender() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로필 이름과 성별을 먼저 설정해주세요.");
        }
        return ParticipantResolution.guest(
                user.getNickName().trim(),
                Gender.valueOf(user.getGender().name())
        );
    }

    private Optional<MeetingAttendance> findGuestToPromote(Meeting meeting, ParticipantResolution participant) {
        if (participant.type() != MeetingParticipantType.CLUB_MEMBER) {
            return Optional.empty();
        }

        return attendanceRepository.findByMeetingAndParticipantNameAndGenderAndDeletedAtIsNull(
                        meeting,
                        participant.name(),
                        participant.gender()
                )
                .filter(attendance -> attendance.getParticipantType() == MeetingParticipantType.GUEST);
    }

    private ClubSkillTier resolveManagedGuestSkillTier(
            Meeting meeting,
            ParticipantResolution participant,
            Long clubSkillTierId
    ) {
        if (clubSkillTierId == null) {
            return null;
        }
        if (meeting.getClubId() == null || participant.type() != MeetingParticipantType.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게스트 등급은 클럽 모임에서만 선택할 수 있습니다.");
        }

        Club club = findActiveClub(meeting.getClubId());
        return clubSkillTierRepository.findByIdAndClub(clubSkillTierId, club)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽에 속하지 않은 등급입니다."));
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
    private ParticipantResolution clubMemberResolution(ClubMember member) {
        ClubSkillTier skillTier = member.getSkillTier();
        return ParticipantResolution.clubMember(
                member.getName(),
                member.getGender(),
                member.getId(),
                skillTier == null ? null : skillTier.getId(),
                skillTier == null ? null : skillTier.getName()
        );
    }

    private void applyParticipantSkillTier(
            MeetingAttendance attendance,
            ParticipantResolution participant,
            ClubSkillTier guestSkillTier
    ) {
        Long skillTierId = guestSkillTier != null ? guestSkillTier.getId() : participant.clubSkillTierId();
        if (skillTierId == null) {
            attendance.clearClubSkillTier();
            return;
        }
        attendance.assignClubSkillTier(skillTierId);
    }

    private Map<Long, String> skillTierNames(
            ParticipantResolution participant,
            ClubSkillTier guestSkillTier
    ) {
        if (guestSkillTier != null) {
            return Map.of(guestSkillTier.getId(), guestSkillTier.getName());
        }
        if (participant.clubSkillTierId() != null) {
            return Map.of(participant.clubSkillTierId(), participant.clubSkillTierName());
        }
        return Map.of();
    }

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
