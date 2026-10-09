package com.tennisfolio.Tennisfolio.club.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tb_club_account",
        uniqueConstraints = @UniqueConstraint(name = "UK_CLUB_ACCOUNT_CLUB_ACCOUNT", columnNames = {"CLUB_ID", "ACCOUNT_ID"})
)
@Getter
@NoArgsConstructor
public class ClubAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_ACCOUNT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLUB_ID", nullable = false)
    private Club club;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "DISPLAY_NAME", nullable = false, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private ClubAccountRole role;

    public ClubAccount(Club club, Long accountId, String displayName, ClubAccountRole role) {
        this.club = club;
        this.accountId = accountId;
        this.displayName = displayName;
        this.role = role;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void changeRole(ClubAccountRole role) {
        this.role = role;
    }
}
