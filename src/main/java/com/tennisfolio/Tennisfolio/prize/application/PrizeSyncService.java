package com.tennisfolio.Tennisfolio.prize.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.player.domain.Player;
import com.tennisfolio.Tennisfolio.player.domain.PlayerAggregate;
import com.tennisfolio.Tennisfolio.player.repository.PlayerRepository;
import com.tennisfolio.Tennisfolio.prize.domain.PlayerPrize;
import com.tennisfolio.Tennisfolio.prize.repository.PrizeRepository;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Builder
public class PrizeSyncService {
    private final PrizeRepository prizeRepository;
    private final PlayerRepository playerRepository;
    private final ApiWorker apiWorker;

    public PrizeSyncService(PrizeRepository prizeRepository, PlayerRepository playerRepository, ApiWorker apiWorker) {
        this.prizeRepository = prizeRepository;
        this.playerRepository = playerRepository;
        this.apiWorker = apiWorker;
    }

    public void savePlayerPrize(){
        List<Player> playerList = playerRepository.findSinglePlayer();
        List<PlayerPrize> prizeList = prizeRepository.findAll();

        playerList.stream().forEach(p -> {
            PlayerAggregate playerAggregate = apiWorker.process(RapidApi.TEAMDETAILS, p.getRapidPlayerId());
            PlayerPrize findPrize = prizeList.stream().filter(pp -> pp.getPlayer().getPlayerId().equals(p.getPlayerId())).findFirst()
                    .orElse(null);

            PlayerPrize newPrize = playerAggregate.getPlayerPrize();

            try{
                if(findPrize == null) {
                    newPrize.updatePlayer(p);
                    prizeRepository.collect(newPrize);
                }
                else {
                    findPrize.updatePlayerPrize(newPrize.getPrizeCurrentAmount(),
                            newPrize.getPrizeCurrentCurrency(),
                            newPrize.getPrizeTotalAmount(),
                            newPrize.getPrizeTotalCurrency());
                    prizeRepository.collect(findPrize);
                }
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("playerId : " + p.getPlayerId());
            }

            prizeRepository.flushWhenFull();
        });

        prizeRepository.flushAll();
    }

}
