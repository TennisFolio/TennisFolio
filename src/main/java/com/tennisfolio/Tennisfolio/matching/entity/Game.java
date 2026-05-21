package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_game")
@Getter
@NoArgsConstructor
public class Game extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GAME_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPETITION_ID", nullable = false)
    private Competition competition;

    @Column(name = "ROUND", nullable = false)
    private Integer round;

    @Column(name = "COURT", nullable = false)
    private Integer court;

    @Column(name = "TEAM_A_SCORE")
    private Integer teamAScore = 0;

    @Column(name = "TEAM_B_SCORE")
    private Integer teamBScore = 0;

    @Column(name = "TEAM_A_TIEBREAK_SCORE")
    private Integer teamATiebreaKScore = 0;

    @Column(name = "TEAM_B_TIEBREAK_SCORE")
    private Integer teamBTiebreaKScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "MATCH_TYPE", nullable = false)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private GameStatus status = GameStatus.READY;


    public Game(Competition competition, Integer round, Integer court, MatchType matchType) {
        this.competition = competition;
        this.round = round;
        this.court = court;
        this.matchType = matchType;
    }

    public void recordScore(Integer teamAScore, Integer teamBScore,
                           Integer teamATiebreaKScore, Integer teamBTiebreaKScore) {
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        this.teamATiebreaKScore = teamATiebreaKScore;
        this.teamBTiebreaKScore = teamBTiebreaKScore;
    }

    public void updateMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public void assignCourt(Integer court) {
        this.court = court;
    }

    public void complete() {
        this.status = GameStatus.COMPLETED;
    }

    @PrePersist
    public void prePersist() {
        if (teamAScore == null) {
            teamAScore = 0;
        }
        if (teamBScore == null) {
            teamBScore = 0;
        }
        if (teamATiebreaKScore == null) {
            teamATiebreaKScore = 0;
        }
        if (teamBTiebreaKScore == null) {
            teamBTiebreaKScore = 0;
        }
    }

    public enum MatchType {
        MIXED, MALE, FEMALE, M2F2_SPLIT, RANDOM_M3F1, RANDOM_M1F3
    }

    public enum GameStatus {
        READY, COMPLETED
    }
}

