package com.tennisfolio.Tennisfolio.prize.domain;

import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.dto.TeamDetailsApiDTO;
import com.tennisfolio.Tennisfolio.prize.repository.PlayerPrizeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static com.tennisfolio.Tennisfolio.util.FiledUpdateUtil.updated;

@Getter
@Builder
@AllArgsConstructor
public class PlayerPrize {

    private Long prizeId;

    private Player player;

    private Long prizeCurrentAmount;

    private String prizeCurrentCurrency;

    private Long prizeTotalAmount;

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

    public void updatePlayerPrize(PlayerPrizeEntity prize){
        this.player = updated(this.player, prize.getPlayerEntity().toModel());
        this.prizeCurrentAmount = updated(this.prizeCurrentAmount, prize.getPrizeCurrentAmount());
        this.prizeCurrentCurrency = updated(this.prizeCurrentCurrency, prize.getPrizeCurrentCurrency());
        this.prizeTotalAmount = updated(this.prizeTotalAmount, prize.getPrizeTotalAmount());
        this.prizeTotalCurrency = updated(this.prizeTotalCurrency, prize.getPrizeTotalCurrency());
    }
}
