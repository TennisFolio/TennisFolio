package com.tennisfolio.Tennisfolio.round.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.round.application.RoundSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/round")
public class RoundController {

    private final RoundSyncService roundSyncService;

    public RoundController( RoundSyncService roundSyncService) {
        this.roundSyncService = roundSyncService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDTO> saveRound(){
        roundSyncService.saveRoundList();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
