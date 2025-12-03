package com.tennisfolio.Tennisfolio.prize.api;

import com.tennisfolio.Tennisfolio.prize.application.PrizeSyncService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlayerPrizeController {
    private final PrizeSyncService prizeSyncService;

    public PlayerPrizeController(PrizeSyncService prizeSyncService) {
        this.prizeSyncService = prizeSyncService;
    }

    @PostMapping("/prize")
    public void savePrize(){
        prizeSyncService.savePlayerPrize();
    }
}
