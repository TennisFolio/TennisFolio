package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClubAccountQueryService {
    private final ClubAccountRepository clubAccountRepository;
    private final ClubAccessService clubAccessService;

    public ClubAccountQueryService(
            ClubAccountRepository clubAccountRepository,
            ClubAccessService clubAccessService
    ) {
        this.clubAccountRepository = clubAccountRepository;
        this.clubAccessService = clubAccessService;
    }

    public List<ClubAccountResponse> getAccounts(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        return clubAccountRepository.findByClubOrderByIdAsc(club)
                .stream()
                .map(ClubAccountResponse::from)
                .toList();
    }

    public ClubAccountResponse getAccount(String clubPublicId, Long clubAccountId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        return ClubAccountResponse.from(findAccount(clubAccountId, club));
    }

    public ClubAccountResponse getMyAccount(String clubPublicId, Long currentUserId) {
        return ClubAccountResponse.from(clubAccessService.requireClubAccount(clubPublicId, currentUserId));
    }

    private ClubAccount findAccount(Long clubAccountId, Club club) {
        return clubAccountRepository.findByIdAndClub(clubAccountId, club)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }
}
