package com.tennisfolio.Tennisfolio.ranking.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.ranking.application.RankingService;
import com.tennisfolio.Tennisfolio.ranking.application.RankingSyncService;
import com.tennisfolio.Tennisfolio.ranking.dto.RankingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RankingController {

    private final RankingService rankingService;
    private final RankingSyncService rankingSyncService;

    @Autowired
    public RankingController(RankingService rankingService, RankingSyncService rankingSyncService){
        this.rankingService = rankingService;
        this.rankingSyncService = rankingSyncService;
    }

    @PostMapping("/ranking")
    public ResponseEntity<ResponseDTO> saveAtpRankings(){
        rankingSyncService.saveAtpRanking();
        return new ResponseEntity(ResponseDTO.success(),HttpStatus.OK);
    }

    @GetMapping("/ranking")
    public ResponseEntity<ResponseDTO<List<RankingResponse>>> getAtpRankings(@RequestParam("page") int page, @RequestParam("size") int size){
        List<RankingResponse> res = rankingService.getRanking(page, size);
        return new ResponseEntity(ResponseDTO.success(res), HttpStatus.OK);
    }
}
