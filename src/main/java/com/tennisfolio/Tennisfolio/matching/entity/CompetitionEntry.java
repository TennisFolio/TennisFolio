package com.tennisfolio.Tennisfolio.matching.entity;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_competition_entry")
@Getter
@NoArgsConstructor
public class CompetitionEntry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPETITION_ENTRY_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPETITION_ID", nullable = false)
    private Competition competition;

    @Column(name = "PLAYER_NAME", nullable = false)
    private String playerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private EntryStatus status = EntryStatus.ACTIVE;


    public CompetitionEntry(Competition competition, String playerName, Gender gender) {
        this.competition = competition;
        this.playerName = playerName;
        this.gender = gender;
    }

    public void updatePlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateStatus(EntryStatus status) {
        this.status = status;
    }


    public enum Gender {
        MALE, FEMALE
    }

    public enum EntryStatus {
        ACTIVE, INACTIVE
    }
}

