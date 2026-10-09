package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubCreateResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.meeting.entity.Meeting;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingAttendanceRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ClubCommandService {

    private final ClubRepository clubRepository;
    private final ClubAccountRepository clubAccountRepository;
    private final UserRepository userRepository;
    private final ClubAccessService clubAccessService;
    private final ClubSkillTierService clubSkillTierService;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;

    public ClubCommandService(
            ClubRepository clubRepository,
            ClubAccountRepository clubAccountRepository,
            UserRepository userRepository,
            ClubAccessService clubAccessService,
            ClubSkillTierService clubSkillTierService,
            MeetingRepository meetingRepository,
            MeetingAttendanceRepository meetingAttendanceRepository
    ) {
        this.clubRepository = clubRepository;
        this.clubAccountRepository = clubAccountRepository;
        this.userRepository = userRepository;
        this.clubAccessService = clubAccessService;
        this.clubSkillTierService = clubSkillTierService;
        this.meetingRepository = meetingRepository;
        this.meetingAttendanceRepository = meetingAttendanceRepository;
    }

    @Transactional
    public ClubCreateResponse createClub(ClubCreateRequest request, Long currentUserId) {
        clubAccessService.requireAuthenticated(currentUserId);
        User creator = userRepository.findByIdAndStatus(currentUserId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        String name = requireName(request.getName());

        Club club = clubRepository.save(new Club(name, request.getDescription(), currentUserId));
        clubAccountRepository.save(new ClubAccount(
                club,
                currentUserId,
                requireName(creator.getNickName()),
                ClubAccountRole.OWNER
        ));
        clubSkillTierService.replaceSkillTiers(club, request.getSkillTiers());
        return new ClubCreateResponse(club.getPublicId());
    }

    @Transactional
    public void updateClub(String clubPublicId, ClubUpdateRequest request, Long currentUserId) {
        Club club = clubAccessService.requireOperator(clubPublicId, currentUserId);
        club.update(requireName(request.getName()), request.getDescription());
        clubSkillTierService.replaceSkillTiers(club, request.getSkillTiers());
    }

    @Transactional
    public void deleteClub(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        LocalDateTime deletedAt = LocalDateTime.now();
        deleteClubMeetings(club.getId(), deletedAt);
        club.delete(deletedAt);
    }

    private void deleteClubMeetings(Long clubId, LocalDateTime deletedAt) {
        for (Meeting meeting : meetingRepository.findByClubIdAndDeletedAtIsNullOrderByStartAtDescIdDesc(clubId)) {
            meetingAttendanceRepository.findByMeetingAndDeletedAtIsNullOrderByIdAsc(meeting)
                    .forEach(attendance -> attendance.delete(deletedAt));
            meeting.delete(deletedAt);
        }
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해주세요.");
        }
        return name.trim();
    }
}
