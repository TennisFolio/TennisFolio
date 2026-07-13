package com.tennisfolio.Tennisfolio.matching.service;

import com.tennisfolio.Tennisfolio.club.service.ClubAccessService;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.matching.entity.Competition;
import com.tennisfolio.Tennisfolio.matching.repository.CompetitionRepository;
import com.tennisfolio.Tennisfolio.meeting.repository.MeetingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
public class CompetitionAdminAuthorizationService {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\\d{4,6}");

    private final CompetitionRepository competitionRepository;
    private final CompetitionAdminTokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MeetingRepository meetingRepository;
    private final ClubAccessService clubAccessService;

    public CompetitionAdminAuthorizationService(
            CompetitionRepository competitionRepository,
            CompetitionAdminTokenService tokenService,
            BCryptPasswordEncoder passwordEncoder,
            MeetingRepository meetingRepository,
            ClubAccessService clubAccessService
    ) {
        this.competitionRepository = competitionRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.meetingRepository = meetingRepository;
        this.clubAccessService = clubAccessService;
    }

    @Transactional
    public String setAdminPassword(String publicId, String adminToken, String password) {
        return setAdminPassword(publicId, getCurrentUserId(), adminToken, password);
    }

    @Transactional
    public String setAdminPassword(String publicId, Long currentUserId, String adminToken, String password) {
        Competition competition = competitionRepository.findByPublicIdAndDeletedAtIsNullForUpdate(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
        if (competition.hasAdminPassword()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "관리자 비밀번호가 이미 설정되었습니다.");
        }
        validateManagementAccess(competition, currentUserId, adminToken);
        validatePasswordFormat(password);
        competition.setAdminPasswordHash(passwordEncoder.encode(password));
        return tokenService.createToken(publicId);
    }

    @Transactional(readOnly = true)
    public String login(String publicId, String password) {
        Competition competition = findCompetition(publicId);
        if (!competition.hasAdminPassword()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "아직 관리자가 관리자 비밀번호를 설정하지 않았습니다.");
        }
        if (!passwordEncoder.matches(password, competition.getAdminPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 비밀번호가 올바르지 않습니다.");
        }
        return tokenService.createToken(publicId);
    }

    public void validateManagementAccess(Competition competition, Long currentUserId, String adminToken) {
        if (canManageByIdentity(competition, currentUserId)) {
            return;
        }
        if (adminToken == null || adminToken.isBlank()) {
            HttpStatus status = currentUserId == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
            throw new ResponseStatusException(status, "Competition management access is required");
        }
        String tokenPublicId = tokenService.validateAndGetPublicId(adminToken);
        if (!competition.getPublicId().equals(tokenPublicId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Competition management access is invalid");
        }
    }

    public boolean canManageByIdentity(Competition competition, Long currentUserId) {
        if (currentUserId == null) {
            return false;
        }
        if (currentUserId.equals(competition.getOwnerUserId())) {
            return true;
        }
        return meetingRepository.findByCompetitionIdAndDeletedAtIsNull(competition.getId())
                .filter(meeting -> meeting.getClubId() != null)
                .map(meeting -> clubAccessService.isActiveAdmin(meeting.getClubId(), currentUserId))
                .orElse(false);
    }

    public void validateAdminToken(String publicId, String adminToken) {
        if (isCurrentUserOwner(publicId)) {
            return;
        }

        String tokenPublicId = tokenService.validateAndGetPublicId(adminToken);
        if (!publicId.equals(tokenPublicId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 올바르지 않습니다. 다시 로그인해 주세요.");
        }
    }

    private boolean isCurrentUserOwner(String publicId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .map(competition -> currentUserId.equals(competition.getOwnerUserId()))
                .orElse(false);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }

        return null;
    }

    private void validatePasswordFormat(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 비밀번호는 4~6자리 숫자로 입력해 주세요.");
        }
    }

    private Competition findCompetition(String publicId) {
        return competitionRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }
}
