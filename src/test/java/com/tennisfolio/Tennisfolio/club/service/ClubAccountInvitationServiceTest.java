package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationAcceptRequest;
import com.tennisfolio.Tennisfolio.club.dto.ClubAccountInvitationIssueResponse;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountInvitation;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountInvitationRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubAccountRepository;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.Gender;
import com.tennisfolio.Tennisfolio.user.domain.User;
import com.tennisfolio.Tennisfolio.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClubAccountInvitationServiceTest {

    @Mock ClubAccountInvitationRepository invitationRepository;
    @Mock ClubAccountRepository clubAccountRepository;
    @Mock ClubAccessService clubAccessService;
    @Mock UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    ClubAccountInvitationService service;

    @BeforeEach
    void setUp() {
        service = new ClubAccountInvitationService(
                invitationRepository,
                clubAccountRepository,
                clubAccessService,
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void issueInvitation_createsThreeHourReusableTokenForOwner() {
        Club club = club();
        when(clubAccessService.requireOwner("club", 10L)).thenReturn(club);
        when(invitationRepository.findByClub(club)).thenReturn(Optional.empty());

        ClubAccountInvitationIssueResponse response = service.issueInvitation("club", 10L);

        ArgumentCaptor<ClubAccountInvitation> invitationCaptor = ArgumentCaptor.forClass(ClubAccountInvitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());
        ClubAccountInvitation invitation = invitationCaptor.getValue();
        assertThat(response.getToken()).contains(".");
        assertThat(response.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(2).plusMinutes(59));
        assertThat(invitation.getTokenId()).isEqualTo(response.getToken().split("\\.")[0]);
        assertThat(passwordEncoder.matches(response.getToken().split("\\.")[1], invitation.getTokenHash())).isTrue();
    }

    @Test
    void acceptInvitation_createsAdminAccountWithSubmittedDisplayName() {
        Club club = club();
        String tokenId = "token-id";
        String secret = "very-long-secret";
        ClubAccountInvitation invitation = invitation(club, tokenId, passwordEncoder.encode(secret), LocalDateTime.now().plusHours(3));
        when(invitationRepository.findByTokenId(tokenId)).thenReturn(Optional.of(invitation));
        when(clubAccountRepository.findByClubAndAccountId(club, 20L)).thenReturn(Optional.empty());
        when(userRepository.findByIdAndStatus(20L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(20L, "Jamie")));
        when(clubAccountRepository.save(any(ClubAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.acceptInvitation(tokenId + "." + secret, new ClubAccountInvitationAcceptRequest("  Club Jamie  "), 20L);

        ArgumentCaptor<ClubAccount> accountCaptor = ArgumentCaptor.forClass(ClubAccount.class);
        verify(clubAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getRole()).isEqualTo(ClubAccountRole.ADMIN);
        assertThat(accountCaptor.getValue().getDisplayName()).isEqualTo("Club Jamie");
    }

    @Test
    void acceptInvitation_rejectsExpiredLink() {
        ClubAccountInvitation invitation = invitation(club(), "token-id", passwordEncoder.encode("secret"), LocalDateTime.now().minusSeconds(1));
        when(invitationRepository.findByTokenId("token-id")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.acceptInvitation(
                "token-id.secret", new ClubAccountInvitationAcceptRequest("Jamie"), 20L
        )).isInstanceOfSatisfying(ResponseStatusException.class, error ->
                assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        );
    }

    @Test
    void acceptInvitation_preservesExistingAccountsRole() {
        Club club = club();
        String tokenId = "token-id";
        String secret = "secret";
        ClubAccountInvitation invitation = invitation(club, tokenId, passwordEncoder.encode(secret), LocalDateTime.now().plusHours(3));
        ClubAccount owner = new ClubAccount(club, 20L, "Jamie", ClubAccountRole.OWNER);
        when(invitationRepository.findByTokenId(tokenId)).thenReturn(Optional.of(invitation));
        when(clubAccountRepository.findByClubAndAccountId(club, 20L)).thenReturn(Optional.of(owner));

        service.acceptInvitation(tokenId + "." + secret, new ClubAccountInvitationAcceptRequest("New name"), 20L);

        assertThat(owner.getRole()).isEqualTo(ClubAccountRole.OWNER);
        assertThat(owner.getDisplayName()).isEqualTo("Jamie");
    }

    @Test
    void acceptInvitation_rejectsBlankDisplayNameEvenForExistingAccount() {
        assertThatThrownBy(() -> service.acceptInvitation(
                "token-id.secret", new ClubAccountInvitationAcceptRequest(" "), 20L
        )).isInstanceOfSatisfying(ResponseStatusException.class, error ->
                assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        );
    }

    private static Club club() {
        Club club = new Club("Morning Tennis", null, 10L);
        ReflectionTestUtils.setField(club, "id", 100L);
        return club;
    }

    private static ClubAccountInvitation invitation(Club club, String tokenId, String tokenHash, LocalDateTime expiresAt) {
        return new ClubAccountInvitation(club, tokenId, tokenHash, expiresAt);
    }

    private static User user(Long userId, String nickName) {
        return User.builder()
                .userId(userId)
                .email("jamie@example.com")
                .nickName(nickName)
                .gender(Gender.FEMALE)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
