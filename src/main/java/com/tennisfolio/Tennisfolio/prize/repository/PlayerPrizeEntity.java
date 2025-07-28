package com.tennisfolio.Tennisfolio.prize.repository;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.repository.CountryEntity;
import com.tennisfolio.Tennisfolio.player.repository.PlayerEntity;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.tennisfolio.Tennisfolio.util.FiledUpdateUtil.updated;

@Entity
@Table(name = "tb_player_prize")
@Getter
@NoArgsConstructor
public class PlayerPrizeEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRIZE_ID")
    private Long prizeId;
    @JoinColumn(name="PLAYER_ID")
    @OneToOne
    private PlayerEntity playerEntity;
    @Column(name="PRIZE_CURRENT_AMOUNT")
    private Long prizeCurrentAmount;
    @Column(name="PRIZE_CURRENT_CURRENCY")
    private String prizeCurrentCurrency;
    @Column(name="PRIZE_TOTAL_AMOUNT")
    private Long prizeTotalAmount;
    @Column(name="PRIZE_TOTAL_CURRENCY")
    private String prizeTotalCurrency;

    public static PlayerPrizeEntity fromModel(PlayerPrize playerPrize) {
        PlayerPrizeEntity playerPrizeEntity = new PlayerPrizeEntity();
        playerPrizeEntity.prizeId = playerPrize.getPrizeId();
        playerPrizeEntity.playerEntity = PlayerEntity.fromModel(playerPrize.getPlayer());
        playerPrizeEntity.prizeCurrentAmount = playerPrize.getPrizeCurrentAmount();
        playerPrizeEntity.prizeCurrentCurrency = playerPrize.getPrizeCurrentCurrency();
        playerPrizeEntity.prizeTotalAmount = playerPrize.getPrizeTotalAmount();
        playerPrizeEntity.prizeTotalCurrency = playerPrize.getPrizeTotalCurrency();

        return playerPrizeEntity;
    }

    public PlayerPrize toModel(){
        return PlayerPrize.builder()
                .prizeId(prizeId)
                .player(playerEntity.toModel())
                .prizeCurrentAmount(prizeCurrentAmount)
                .prizeCurrentCurrency(prizeCurrentCurrency)
                .prizeTotalAmount(prizeTotalAmount)
                .prizeTotalCurrency(prizeTotalCurrency)
                .build();
    }



}
