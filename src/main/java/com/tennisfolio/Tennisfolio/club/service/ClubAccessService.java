package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubMember;
import com.tennisfolio.Tennisfolio.club.entity.ClubMemberRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClubAccessService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    public ClubAccessService(
            ClubRepository clubRepository,
            ClubMemberRepository clubMemberRepository
    ) {
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
    }

    public Club requireAdmin(String clubPublicId, Long currentUserId) {
        requireAuthenticated(currentUserId);
        Club club = findActiveClub(clubPublicId);
        ClubMember member = findActiveMember(club, currentUserId);
        if (member.getRole() != ClubMemberRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 관리자만 사용할 수 있습니다.");
        }
        return club;
    }

    public ClubMember requireActiveMember(String clubPublicId, Long currentUserId) {
        requireAuthenticated(currentUserId);
        Club club = findActiveClub(clubPublicId);
        return findActiveMember(club, currentUserId);
    }

    public Club findActiveClub(String clubPublicId) {
        return clubRepository.findByPublicIdAndDeletedAtIsNull(clubPublicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    public void requireAuthenticated(Long currentUserId) {
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }

    private ClubMember findActiveMember(Club club, Long currentUserId) {
        return clubMemberRepository.findByClubAndUserIdAndActiveTrue(club, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 멤버만 사용할 수 있습니다."));
    }
}
