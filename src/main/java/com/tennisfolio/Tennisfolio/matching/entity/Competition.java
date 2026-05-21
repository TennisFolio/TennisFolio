package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "EDIT_TOKEN", nullable = false, updatable = false, length = 36)
    private String editToken = UUID.randomUUID().toString();

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


    public Competition(String name, Integer maleCount, Integer femaleCount,
                      Integer courtCount, Integer rounds, Long seed, CompetitionMode mode) {
        this.name = name;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.courtCount = courtCount;
        this.rounds = rounds;
        this.seed = seed;
        this.mode = mode == null ? CompetitionMode.FIXED_SCHEDULE : mode;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateCourtCount(Integer courtCount) {
        this.courtCount = courtCount;
    }

    public void incrementMaleCount() {
        this.maleCount++;
    }

    public void incrementFemaleCount() {
        this.femaleCount++;
    }


    public enum CompetitionStatus {
        READY, INPROGRESS, COMPLETED
    }

    public enum CompetitionMode {
        CLUB_SESSION, FIXED_SCHEDULE
    }
}

