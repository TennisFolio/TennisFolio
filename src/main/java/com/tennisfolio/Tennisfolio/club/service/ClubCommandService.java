package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubCreateResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
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
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final ClubAccessService clubAccessService;

    public ClubCommandService(
            ClubRepository clubRepository,
            ClubMemberRepository clubMemberRepository,
            UserRepository userRepository,
            ClubAccessService clubAccessService
    ) {
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.userRepository = userRepository;
        this.clubAccessService = clubAccessService;
    }

    @Transactional
    public ClubCreateResponse createClub(ClubCreateRequest request, Long currentUserId) {
        clubAccessService.requireAuthenticated(currentUserId);
        User creator = userRepository.findByIdAndStatus(currentUserId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        String name = requireName(request.getName());

        Club club = clubRepository.save(new Club(name, request.getDescription(), currentUserId));
        clubMemberRepository.save(new ClubMember(
                club,
                currentUserId,
                requireName(creator.getNickName()),
                Gender.valueOf(creator.getGender().name()),
                ClubMemberRole.ADMIN,
                null,
                null,
                null
        ));
        return new ClubCreateResponse(club.getPublicId());
    }

    @Transactional
    public void updateClub(String clubPublicId, ClubUpdateRequest request, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        club.update(requireName(request.getName()), request.getDescription());
    }

    @Transactional
    public void deleteClub(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        club.delete(LocalDateTime.now());
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해주세요.");
        }
        return name.trim();
    }
}
