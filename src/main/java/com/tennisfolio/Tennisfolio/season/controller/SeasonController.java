package com.tennisfolio.Tennisfolio.season.controller;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.season.service.SeasonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/season")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }
    @PostMapping("")
    public ResponseEntity<ResponseDTO<Void>> saveSeasonList(){
        seasonService.saveSeasonList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/info")
    public ResponseEntity<ResponseDTO<Void>> saveSeasonInfo(){
        seasonService.saveSeasonInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
