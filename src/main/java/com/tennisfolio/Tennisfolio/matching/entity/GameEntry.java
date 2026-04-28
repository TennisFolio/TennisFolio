package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_game_entry")
@Getter
@NoArgsConstructor
public class GameEntry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_ID", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPETITION_ENTRY_ID", nullable = false)
    private CompetitionEntry competitionEntry;

    @Enumerated(EnumType.STRING)
    @Column(name = "TEAM", nullable = false)
    private Team team;

    @Column(name = "POSITION")
    private Integer position;

    public GameEntry(Game game, CompetitionEntry competitionEntry, Team team) {
        this.game = game;
        this.competitionEntry = competitionEntry;
        this.team = team;
    }

    public GameEntry(Game game, CompetitionEntry competitionEntry, Team team, Integer position) {
        this.game = game;
        this.competitionEntry = competitionEntry;
        this.team = team;
        this.position = position;
    }

    public enum Team {
        A, B
    }
}

