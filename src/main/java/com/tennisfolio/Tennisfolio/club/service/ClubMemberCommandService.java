package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClubMemberCommandService {

    private final ClubMemberRepository clubMemberRepository;
    private final ClubAccessService clubAccessService;

    public ClubMemberCommandService(
            ClubMemberRepository clubMemberRepository,
            ClubAccessService clubAccessService
    ) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubAccessService = clubAccessService;
    }

    @Transactional
    public void addMember(String clubPublicId, ClubMemberCreateRequest request, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        String name = requireName(request.getName());
        rejectDuplicateName(club, name);
        clubMemberRepository.save(new ClubMember(
                club,
                null,
                name,
                parseGender(request.getGender()),
                parseRole(request.getRole()),
                request.getSkillNote(),
                request.getContactMemo(),
                request.getMemo()
        ));
    }

    @Transactional
    public void updateMember(
            String clubPublicId,
            Long memberId,
            ClubMemberUpdateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        ClubMember member = findActiveMember(club, memberId);
        String name = requireName(request.getName());
        rejectDuplicateNameExceptSelf(club, name, member.getId());
        ClubMemberRole requestedRole = parseRole(request.getRole());
        ensureAdminCanChange(member, requestedRole);
        member.update(
                name,
                parseGender(request.getGender()),
                requestedRole,
                request.getSkillNote(),
                request.getContactMemo(),
                request.getMemo()
        );
    }

    @Transactional
    public void deleteMember(String clubPublicId, Long memberId, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        ClubMember member = findActiveMember(club, memberId);
        ensureAdminCanChange(member, ClubMemberRole.MEMBER);
        member.deactivate();
    }

    private ClubMember findActiveMember(Club club, Long memberId) {
        return clubMemberRepository.findByClubAndIdAndActiveTrue(club, memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private void rejectDuplicateName(Club club, String name) {
        if (clubMemberRepository.existsByClubAndNameAndActiveTrue(club, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 이름의 클럽원이 있습니다.");
        }
    }

    private void rejectDuplicateNameExceptSelf(Club club, String name, Long memberId) {
        if (clubMemberRepository.existsByClubAndNameAndActiveTrueAndIdNot(club, name, memberId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 같은 이름의 클럽원이 있습니다.");
        }
    }

    private void ensureAdminCanChange(ClubMember member, ClubMemberRole requestedRole) {
        if (member.getRole() != ClubMemberRole.ADMIN || requestedRole == ClubMemberRole.ADMIN) {
            return;
        }
        long activeAdminCount = clubMemberRepository.countByClubAndRoleAndActiveTrue(
                member.getClub(),
                ClubMemberRole.ADMIN
        );
        if (activeAdminCount <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 ADMIN은 삭제하거나 일반 멤버로 변경할 수 없습니다.");
        }
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이름을 입력해주세요.");
        }
        return name.trim();
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "성별 값이 올바르지 않습니다.");
        }
    }

    private ClubMemberRole parseRole(String role) {
        try {
            return ClubMemberRole.valueOf(role);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽원 역할 값이 올바르지 않습니다.");
        }
    }
}
