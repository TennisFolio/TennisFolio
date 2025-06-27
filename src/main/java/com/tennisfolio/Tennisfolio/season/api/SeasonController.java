package com.tennisfolio.Tennisfolio.season.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.season.application.SeasonSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/season")
public class SeasonController {

    private final SeasonSyncService seasonSyncService;

    public SeasonController(SeasonSyncService seasonSyncService) {
        this.seasonSyncService = seasonSyncService;
    }
    @PostMapping("")
    public ResponseEntity<ResponseDTO<Void>> saveSeasonList(){
        seasonSyncService.saveSeasonList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/info")
    public ResponseEntity<ResponseDTO<Void>> saveSeasonInfo(){
        seasonSyncService.saveSeasonInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
