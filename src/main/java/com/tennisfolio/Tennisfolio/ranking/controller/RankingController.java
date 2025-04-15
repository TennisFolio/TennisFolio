package com.tennisfolio.Tennisfolio.ranking.controller;

import com.tennisfolio.Tennisfolio.ranking.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RankingController {

    private final RankingService rankingService;

    @Autowired
    public RankingController(RankingService rankingService){
        this.rankingService = rankingService;
    }

    @PostMapping("/ranking")
    public ResponseEntity atpRankings(){
        rankingService.atpRanking();
        return new ResponseEntity(HttpStatus.OK);
    }
}
