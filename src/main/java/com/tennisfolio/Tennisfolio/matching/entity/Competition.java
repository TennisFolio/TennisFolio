package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_competition")
@Getter
@NoArgsConstructor
public class Competition extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPETITION_ID")
    private Long id;

    @Column(name = "PUBLIC_ID", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId = UUID.randomUUID().toString();

    @Column(name = "ADMIN_PASSWORD_HASH", length = 100)
    private String adminPasswordHash;

    @Column(name = "COMPETITION_NAME", nullable = false)
    private String name;

    @Column(name = "MALE_COUNT", nullable = false)
    private Integer maleCount;

    @Column(name = "FEMALE_COUNT", nullable = false)
    private Integer femaleCount;

    @Column(name = "COURT_COUNT", nullable = false)
    private Integer courtCount;

    @Column(name = "ROUNDS", nullable = false)
    private Integer rounds;

    @Column(name = "SEED", nullable = false)
    private Long seed;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private CompetitionStatus status = CompetitionStatus.READY;

    @Enumerated(EnumType.STRING)
    @Column(name = "MODE", nullable = false)
    private CompetitionMode mode = CompetitionMode.FIXED_SCHEDULE;

    @Column(name = "IS_MODIFIED", nullable = false)
    private Boolean isModified = false;

    @Column(name = "OWNER_USER_ID")
    private Long ownerUserId;

    @Column(name = "DEL_DT")
    private LocalDateTime deletedAt;


    public Competition(String name, Integer maleCount, Integer femaleCount,
                      Integer courtCount, Integer rounds, Long seed, CompetitionMode mode) {
        this(name, maleCount, femaleCount, courtCount, rounds, seed, mode, null);
    }

    public Competition(String name, Integer maleCount, Integer femaleCount,
                      Integer courtCount, Integer rounds, Long seed, CompetitionMode mode, Long ownerUserId) {
        this.name = name;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.courtCount = courtCount;
        this.rounds = rounds;
        this.seed = seed;
        this.mode = mode == null ? CompetitionMode.FIXED_SCHEDULE : mode;
        this.ownerUserId = ownerUserId;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateCourtCount(Integer courtCount) {
        this.courtCount = courtCount;
    }

    public boolean hasAdminPassword() {
        return adminPasswordHash != null && !adminPasswordHash.isBlank();
    }

    public void setAdminPasswordHash(String adminPasswordHash) {
        this.adminPasswordHash = adminPasswordHash;
    }

    public void incrementMaleCount() {
        this.maleCount++;
    }

    public void incrementFemaleCount() {
        this.femaleCount++;
    }

    public void delete(LocalDateTime deletedAt) {
        if (this.deletedAt == null) {
            this.deletedAt = deletedAt;
        }
    }

    public void claimOwner(Long ownerUserId) {
        if (this.ownerUserId == null) {
            this.ownerUserId = ownerUserId;
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }


    public enum CompetitionStatus {
        READY, INPROGRESS, COMPLETED
    }

    public enum CompetitionMode {
        CLUB_SESSION, FIXED_SCHEDULE
    }
}

