package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountRoleUpdateRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ClubAccountCommandService {
    private final ClubAccountRepository clubAccountRepository;
    private final ClubAccessService clubAccessService;

    public ClubAccountCommandService(
            ClubAccountRepository clubAccountRepository,
            ClubAccessService clubAccessService
    ) {
        this.clubAccountRepository = clubAccountRepository;
        this.clubAccessService = clubAccessService;
    }

    public void changeRole(
            String clubPublicId,
            Long clubAccountId,
            ClubAccountRoleUpdateRequest request,
            Long currentUserId
    ) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        ClubAccount account = findAccount(clubAccountId, club);
        ClubAccountRole role = parseRole(request.getRole());
        preventRemovingLastOwner(account, role);
        account.changeRole(role);
    }

    public void removeAccount(String clubPublicId, Long clubAccountId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        ClubAccount account = findAccount(clubAccountId, club);
        preventRemovingLastOwner(account);
        clubAccountRepository.delete(account);
    }

    public void leaveMyAccount(String clubPublicId, Long currentUserId) {
        ClubAccount account = clubAccessService.requireClubAccount(clubPublicId, currentUserId);
        preventRemovingLastOwner(account);
        clubAccountRepository.delete(account);
    }

    private ClubAccount findAccount(Long clubAccountId, Club club) {
        return clubAccountRepository.findByIdAndClub(clubAccountId, club)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private ClubAccountRole parseRole(String role) {
        try {
            return ClubAccountRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽 계정 역할 값이 올바르지 않습니다.");
        }
    }

    private void preventRemovingLastOwner(ClubAccount account, ClubAccountRole nextRole) {
        if (account.getRole() == ClubAccountRole.OWNER
                && nextRole != ClubAccountRole.OWNER) {
            preventRemovingLastOwner(account);
        }
    }

    private void preventRemovingLastOwner(ClubAccount account) {
        if (account.getRole() == ClubAccountRole.OWNER
                && clubAccountRepository.countByClubAndRole(account.getClub(), ClubAccountRole.OWNER) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 OWNER는 변경하거나 클럽에서 탈퇴할 수 없습니다.");
        }
    }
}
