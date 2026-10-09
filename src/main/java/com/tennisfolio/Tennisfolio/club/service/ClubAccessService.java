package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClubAccessService {
    private final ClubRepository clubRepository;
    private final ClubAccountRepository clubAccountRepository;

    public ClubAccessService(ClubRepository clubRepository, ClubAccountRepository clubAccountRepository) {
        this.clubRepository = clubRepository;
        this.clubAccountRepository = clubAccountRepository;
    }

    public Club requireOperator(String clubPublicId, Long currentUserId) {
        requireAuthenticated(currentUserId);
        Club club = findActiveClub(clubPublicId);
        findClubAccount(club, currentUserId);
        return club;
    }

    public Club requireOwner(String clubPublicId, Long currentUserId) {
        ClubAccount account = requireClubAccount(clubPublicId, currentUserId);
        if (account.getRole() != ClubAccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 OWNER만 사용할 수 있습니다.");
        }
        return account.getClub();
    }

    public ClubAccount requireClubAccount(String clubPublicId, Long currentUserId) {
        Club club = requireOperator(clubPublicId, currentUserId);
        return findClubAccount(club, currentUserId);
    }

    public boolean isOperator(Long clubId, Long currentUserId) {
        return clubId != null
                && currentUserId != null
                && clubAccountRepository.findByClubIdAndAccountId(clubId, currentUserId).isPresent();
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

    private ClubAccount findClubAccount(Club club, Long currentUserId) {
        return clubAccountRepository.findByClubAndAccountId(club, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "클럽 운영진만 사용할 수 있습니다."));
    }
}
