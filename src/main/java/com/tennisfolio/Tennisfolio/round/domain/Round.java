package com.tennisfolio.Tennisfolio.round.domain;

import com.tennisfolio.Tennisfolio.infrastructure.api.round.leagueRounds.LeagueRoundsDTO;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_round")
@NoArgsConstructor
public class Round {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="ROUND_ID")
    private Long roundId;
    @ManyToOne
    @JoinColumn(name = "SEASON_ID")
    private Season season;
    @Column(name = "ROUND")
    private Long round;
    @Column(name="ROUND_NAME")
    private String name;
    @Column(name="ROUND_SLUG")
    private String slug;

    public Round(LeagueRoundsDTO dto, Season season){
        this.season = season;
        this.round = dto.getRound();
        this.name = dto.getName();
        this.slug = dto.getSlug();
    }
}
