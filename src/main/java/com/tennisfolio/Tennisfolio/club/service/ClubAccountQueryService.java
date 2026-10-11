package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClubAccountQueryService {
    private final ClubAccountRepository clubAccountRepository;
    private final ClubAccessService clubAccessService;
    private final UserRepository userRepository;

    public ClubAccountQueryService(
            ClubAccountRepository clubAccountRepository,
            ClubAccessService clubAccessService,
            UserRepository userRepository
    ) {
        this.clubAccountRepository = clubAccountRepository;
        this.clubAccessService = clubAccessService;
        this.userRepository = userRepository;
    }

    public List<ClubAccountResponse> getAccounts(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        return clubAccountRepository.findByClubOrderByIdAsc(club)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ClubAccountResponse getAccount(String clubPublicId, Long clubAccountId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        return toResponse(findAccount(clubAccountId, club));
    }

    public ClubAccountResponse getMyAccount(String clubPublicId, Long currentUserId) {
        return toResponse(clubAccessService.requireClubAccount(clubPublicId, currentUserId));
    }

    private ClubAccount findAccount(Long clubAccountId, Club club) {
        return clubAccountRepository.findByIdAndClub(clubAccountId, club)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private ClubAccountResponse toResponse(ClubAccount account) {
        User user = userRepository.findByIdAndStatus(account.getAccountId(), UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        return ClubAccountResponse.from(account, user);
    }
}
