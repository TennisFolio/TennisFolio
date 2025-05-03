package com.tennisfolio.Tennisfolio.player.domain;

import com.tennisfolio.Tennisfolio.api.teamdetails.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_player_prize")
@Getter
@Setter
@NoArgsConstructor
public class PlayerPrize extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRIZE_ID")
    private Long prizeId;
    @JoinColumn(name="PLAYER_ID")
    @OneToOne
    private Player player;
    @Column(name="PRIZE_CURRENT_AMOUNT")
    private Long prizeCurrentAmount;
    @Column(name="PRIZE_CURRENT_CURRENCY")
    private String prizeCurrentCurrency;
    @Column(name="PRIZE_TOTAL_AMOUNT")
    private Long prizeTotalAmount;
    @Column(name="PRIZE_TOTAL_CURRENCY")
    private String prizeTotalCurrency;

    public PlayerPrize(TeamDetailsApiDTO dto, Player player){
        this.player = player;
        if(dto.getPrizeCurrent() != null){
            this.prizeCurrentAmount = dto.getPrizeCurrent().getValue();
            this.prizeCurrentCurrency = dto.getPrizeCurrent().getCurrency();
        }
        if(dto.getPrizeTotal() != null){
            this.prizeTotalAmount = dto.getPrizeTotal().getValue();
            this.prizeTotalCurrency =dto.getPrizeTotal().getCurrency();
        }
    }
}
