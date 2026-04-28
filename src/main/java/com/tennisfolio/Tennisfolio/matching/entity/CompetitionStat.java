package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_competition_stat")
@Getter
@Setter
@NoArgsConstructor
public class CompetitionStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPETITION_ID", nullable = false, unique = true)
    private Competition competition;

    @Column(name = "TOTAL_GAMES", nullable = false)
    private Integer totalGames = 0;

    @Column(name = "MIXED_COUNT", nullable = false)
    private Integer mixedCount = 0;

    @Column(name = "MALE_COUNT", nullable = false)
    private Integer maleCount = 0;

    @Column(name = "FEMALE_COUNT", nullable = false)
    private Integer femaleCount = 0;

    @Column(name = "M3F1_COUNT", nullable = false)
    private Integer randomM3F1Count = 0;

    @Column(name = "M1F3_COUNT", nullable = false)
    private Integer randomM1F3Count = 0;

    @Column(name = "MAX_GAMES", nullable = false)
    private Integer maxGames = 0;

    @Column(name = "MIN_GAMES", nullable = false)
    private Integer minGames = 0;

    public CompetitionStat(Competition competition) {
        this.competition = competition;
    }

    public void incrementTotalGames() {
        this.totalGames++;
    }

    public void incrementMixedCount() {
        this.mixedCount++;
    }

    public void incrementMaleCount() {
        this.maleCount++;
    }

    public void incrementFemaleCount() {
        this.femaleCount++;
    }

    public void incrementRandomM3F1Count() {
        this.randomM3F1Count++;
    }

    public void incrementRandomM1F3Count() {
        this.randomM1F3Count++;
    }

    public void updateGameStatistics(int maxGames, int minGames) {
        this.maxGames = maxGames;
        this.minGames = minGames;
    }
}

