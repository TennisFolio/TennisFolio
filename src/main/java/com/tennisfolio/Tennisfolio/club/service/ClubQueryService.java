package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubDetailResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubMemberResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubSkillTierResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubSummaryResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClubQueryService {

    private final ClubMemberRepository clubMemberRepository;
    private final ClubAccessService clubAccessService;
    private final ClubSkillTierRepository clubSkillTierRepository;

    public ClubQueryService(
            ClubMemberRepository clubMemberRepository,
            ClubAccessService clubAccessService,
            ClubSkillTierRepository clubSkillTierRepository
    ) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubAccessService = clubAccessService;
        this.clubSkillTierRepository = clubSkillTierRepository;
    }

    @Transactional(readOnly = true)
    public List<ClubSummaryResponse> getMyClubs(Long currentUserId) {
        clubAccessService.requireAuthenticated(currentUserId);
        return clubMemberRepository.findByUserIdAndActiveTrueOrderByIdAsc(currentUserId)
                .stream()
                .filter(member -> member.getClub().getDeletedAt() == null)
                .map(member -> ClubSummaryResponse.from(
                        member,
                        clubMemberRepository.countByClubAndActiveTrue(member.getClub())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClubDetailResponse getClub(String clubPublicId, Long currentUserId) {
        ClubMember currentMember = clubAccessService.requireActiveMember(clubPublicId, currentUserId);
        Club club = currentMember.getClub();
        return ClubDetailResponse.from(
                club,
                currentMember,
                clubMemberRepository.countByClubAndActiveTrue(club),
                clubSkillTierRepository.findByClubOrderByLevelDescIdAsc(club)
                        .stream()
                        .map(ClubSkillTierResponse::from)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<ClubMemberResponse> getMembers(String clubPublicId, String keyword, Long currentUserId) {
        ClubMember currentMember = clubAccessService.requireActiveMember(clubPublicId, currentUserId);
        Club club = currentMember.getClub();
        return findMembers(club, keyword)
                .stream()
                .map(ClubMemberResponse::from)
                .toList();
    }

    private List<ClubMember> findMembers(Club club, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return clubMemberRepository.findByClubAndActiveTrueOrderByNameAscIdAsc(club);
        }
        return clubMemberRepository.findByClubAndNameContainingIgnoreCaseAndActiveTrueOrderByNameAscIdAsc(
                club,
                keyword.trim()
        );
    }
}
