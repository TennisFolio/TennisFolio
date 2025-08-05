package com.tennisfolio.Tennisfolio.round.repository;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.season.repository.SeasonEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="tb_round")
@NoArgsConstructor
public class RoundEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator = "round_gen")
    @TableGenerator(
            name="round_gen",
            table="TB_SEQUENCES",
            pkColumnName = "TABLE_ID",
            valueColumnName = "NEXT_VAL",
            pkColumnValue = "ROUND_ID",
            allocationSize = 1000
    )
    private Long roundId;
    @ManyToOne
    @JoinColumn(name = "SEASON_ID")
    private SeasonEntity seasonEntity;
    @Column(name = "ROUND")
    private Long round;
    @Column(name="ROUND_NAME")
    private String name;
    @Column(name="ROUND_SLUG")
    private String slug;


    public static RoundEntity fromModel(Round round) {
        RoundEntity roundEntity = new RoundEntity();
        roundEntity.roundId = round.getRoundId();
        roundEntity.seasonEntity = SeasonEntity.fromModel(round.getSeason());
        roundEntity.round = round.getRound();
        roundEntity.name = round.getName();
        roundEntity.slug = round.getSlug();

        return roundEntity;
    }

    public Round toModel(){
        return Round.builder()
                .roundId(roundId)
                .season(seasonEntity.toModel())
                .round(round)
                .name(name)
                .slug(slug)
                .build();
    }
}
