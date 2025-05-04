package com.tennisfolio.Tennisfolio.match.controller;

import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.match.response.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService){
        this.matchService = matchService;
    }
    @GetMapping("/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getLiveEvents(){
        List<LiveMatchResponse> events = matchService.getLiveEvents();

        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }
}
