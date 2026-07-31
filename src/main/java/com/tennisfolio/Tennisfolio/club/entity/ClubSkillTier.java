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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tb_club_skill_tier",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_club_skill_tier_name", columnNames = {"CLUB_ID", "NAME"}),
                @UniqueConstraint(name = "uk_club_skill_tier_level", columnNames = {"CLUB_ID", "LEVEL"})
        }
)
@Getter
@NoArgsConstructor
public class ClubSkillTier extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUB_SKILL_TIER_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLUB_ID", nullable = false)
    private Club club;

    @Column(name = "NAME", nullable = false, length = 10)
    private String name;

    @Column(name = "LEVEL", nullable = false)
    private int level;

    public ClubSkillTier(Club club, String name, int level) {
        this.club = club;
        this.name = name;
        this.level = level;
    }

    public void update(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public void moveLevel(int level) {
        this.level = level;
    }
}
