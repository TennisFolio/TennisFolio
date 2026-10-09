package com.tennisfolio.Tennisfolio.club.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_club_account_invitation")
@Getter
@NoArgsConstructor
public class ClubAccountInvitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_ACCOUNT_INVITATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLUB_ID", nullable = false, unique = true)
    private Club club;

    @Column(name = "TOKEN_ID", nullable = false, unique = true, length = 36)
    private String tokenId;

    @Column(name = "TOKEN_HASH", nullable = false, length = 100)
    private String tokenHash;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    public ClubAccountInvitation(Club club, String tokenId, String tokenHash, LocalDateTime expiresAt) {
        this.club = club;
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void reissue(String tokenId, String tokenHash, LocalDateTime expiresAt) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.closedAt = null;
    }

    public void close(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isActiveAt(LocalDateTime now) {
        return closedAt == null && expiresAt.isAfter(now);
    }
}
