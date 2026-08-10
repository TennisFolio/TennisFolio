package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubMemberCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberBulkCreateRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.meeting.domain.Gender;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClubMemberCommandService {

    private final ClubMemberRepository clubMemberRepository;
    private final ClubSkillTierRepository clubSkillTierRepository;
    private final ClubAccessService clubAccessService;

    public ClubMemberCommandService(
            ClubMemberRepository clubMemberRepository,
            ClubSkillTierRepository clubSkillTierRepository,
            ClubAccessService clubAccessService
    ) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubSkillTierRepository = clubSkillTierRepository;
        this.clubAccessService = clubAccessService;
    }

    @Transactional
    public void addMember(String clubPublicId, ClubMemberCreateRequest request, Long currentUserId) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        String name = requireName(request.getName());
        rejectDuplicateName(club, name);
        clubMemberRepository.save(createMember(club, request, name));
    }

    @Transactional
    public void addMembers(
            String clubPublicId,
            ClubMemberBulkCreateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireAdmin(clubPublicId, currentUserId);
        List<ClubMember> members = createMembers(club, request);
        clubMemberRepository.saveAll(members);
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
        ClubSkillTier skillTier = resolveSkillTier(club, request.getSkillTierId());
        member.update(
                name,
                parseGender(request.getGender()),
                requestedRole,
                skillTier,
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

    private List<ClubMember> createMembers(Club club, ClubMemberBulkCreateRequest request) {
        List<ClubMemberCreateRequest> requests = requireMembers(request);
        Set<String> names = new HashSet<>();
        List<ClubMember> members = new ArrayList<>();

        for (ClubMemberCreateRequest memberRequest : requests) {
            String name = requireName(memberRequest.getName());
            rejectDuplicateNameInRequest(names, name);
            rejectDuplicateName(club, name);
            members.add(createMember(club, memberRequest, name));
        }

        return members;
    }

    private List<ClubMemberCreateRequest> requireMembers(ClubMemberBulkCreateRequest request) {
        if (request == null || request.getMembers() == null || request.getMembers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등록할 클럽원을 입력해 주세요.");
        }
        return request.getMembers();
    }

    private void rejectDuplicateNameInRequest(Set<String> names, String name) {
        if (!names.add(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "입력한 클럽원 이름이 중복됩니다.");
        }
    }

    private ClubMember createMember(Club club, ClubMemberCreateRequest request, String name) {
        ClubSkillTier skillTier = resolveSkillTier(club, request.getSkillTierId());
        return new ClubMember(
                club,
                null,
                name,
                parseGender(request.getGender()),
                parseRole(request.getRole()),
                skillTier,
                request.getContactMemo(),
                request.getMemo()
        );
    }

    private ClubSkillTier resolveSkillTier(Club club, Long skillTierId) {
        if (skillTierId == null) {
            return null;
        }
        return clubSkillTierRepository.findByIdAndClub(skillTierId, club)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽에 속하지 않은 등급입니다."));
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
