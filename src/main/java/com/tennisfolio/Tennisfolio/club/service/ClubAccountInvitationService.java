package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationIssueResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationPreviewResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationStatusResponse;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountInvitation;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountInvitationRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.exception.NotFoundException;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ClubAccountInvitationService {
    private static final int INVITATION_VALID_HOURS = 3;
    private final ClubAccountInvitationRepository invitationRepository;
    private final ClubAccountRepository clubAccountRepository;
    private final ClubAccessService clubAccessService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public ClubAccountInvitationService(
            ClubAccountInvitationRepository invitationRepository,
            ClubAccountRepository clubAccountRepository,
            ClubAccessService clubAccessService,
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.invitationRepository = invitationRepository;
        this.clubAccountRepository = clubAccountRepository;
        this.clubAccessService = clubAccessService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ClubAccountInvitationIssueResponse issueInvitation(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        InvitationToken token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(INVITATION_VALID_HOURS);
        ClubAccountInvitation invitation = invitationRepository.findByClub(club)
                .map(existing -> reissue(existing, token, expiresAt))
                .orElseGet(() -> new ClubAccountInvitation(club, token.id(), passwordEncoder.encode(token.secret()), expiresAt));
        invitationRepository.save(invitation);
        return new ClubAccountInvitationIssueResponse(token.value(), expiresAt);
    }

    public ClubAccountResponse acceptInvitation(
            String rawToken,
            Long currentUserId
    ) {
        clubAccessService.requireAuthenticated(currentUserId);
        InvitationToken token = parseToken(rawToken);
        ClubAccountInvitation invitation = findActiveInvitation(token);
        User user = findActiveUser(currentUserId);
        return clubAccountRepository.findByClubAndAccountId(invitation.getClub(), currentUserId)
                .map(account -> ClubAccountResponse.from(account, user))
                .orElseGet(() -> createAdminAccount(invitation.getClub(), user));
    }

    @Transactional(readOnly = true)
    public ClubAccountInvitationPreviewResponse previewInvitation(String rawToken) {
        InvitationToken token = parseToken(rawToken);
        ClubAccountInvitation invitation = findActiveInvitation(token);
        return new ClubAccountInvitationPreviewResponse(
                invitation.getClub().getName(),
                invitation.getClub().getPublicId()
        );
    }

    public void closeInvitation(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        invitationRepository.findByClub(club)
                .ifPresent(invitation -> invitation.close(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public ClubAccountInvitationStatusResponse getInvitationStatus(String clubPublicId, Long currentUserId) {
        Club club = clubAccessService.requireOwner(clubPublicId, currentUserId);
        ClubAccountInvitation invitation = invitationRepository.findByClub(club)
                .orElseThrow(this::invitationNotFound);
        return ClubAccountInvitationStatusResponse.from(invitation, LocalDateTime.now());
    }

    private ClubAccountInvitation reissue(
            ClubAccountInvitation invitation,
            InvitationToken token,
            LocalDateTime expiresAt
    ) {
        invitation.reissue(token.id(), passwordEncoder.encode(token.secret()), expiresAt);
        return invitation;
    }

    private ClubAccountInvitation findActiveInvitation(InvitationToken token) {
        ClubAccountInvitation invitation = invitationRepository.findByTokenId(token.id())
                .orElseThrow(this::invitationNotFound);
        if (!invitation.isActiveAt(LocalDateTime.now()) || !passwordEncoder.matches(token.secret(), invitation.getTokenHash())) {
            throw invitationNotFound();
        }
        return invitation;
    }

    private ClubAccountResponse createAdminAccount(Club club, User user) {
        ClubAccount account = new ClubAccount(
                club,
                user.getUserId(),
                ClubAccountRole.ADMIN
        );
        return ClubAccountResponse.from(clubAccountRepository.save(account), user);
    }

    private InvitationToken generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return new InvitationToken(
                UUID.randomUUID().toString(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        );
    }

    private InvitationToken parseToken(String rawToken) {
        if (rawToken == null) {
            throw invitationNotFound();
        }
        String[] parts = rawToken.split("\\.", -1);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw invitationNotFound();
        }
        return new InvitationToken(parts[0], parts[1]);
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND));
    }

    private ResponseStatusException invitationNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않거나 만료된 초대 링크입니다.");
    }

    private record InvitationToken(String id, String secret) {
        String value() {
            return id + "." + secret;
        }
    }
}
