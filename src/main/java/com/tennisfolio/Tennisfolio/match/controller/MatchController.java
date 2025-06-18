package com.tennisfolio.Tennisfolio.match.controller;

import com.tennisfolio.Tennisfolio.api.liveEvents.LiveEventsApiDTO;
import com.tennisfolio.Tennisfolio.common.ChatMessage;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.match.response.LiveMatchResponse;
import com.tennisfolio.Tennisfolio.match.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchController(MatchService matchService, SimpMessagingTemplate messagingTemplate){
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }
    @GetMapping("/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getLiveEvents(){
        List<LiveMatchResponse> events = matchService.getLiveEvents();

        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }
    @Scheduled(fixedRate = 30000)
    public void getLiveEventsSchedule(){
        List<LiveMatchResponse> events = matchService.getLiveEvents();
        System.out.println("getLiveEventsSchedule ============ 배포가 됐다 ================");
        messagingTemplate.convertAndSend("/topic/liveMatches", events);
    }

    @GetMapping("/liveEvents/{matchId}")
    public ResponseEntity<ResponseDTO<LiveMatchResponse>> getLiveEvent(@PathVariable("matchId") String matchId){
        System.out.println("getLiveEvent ============ 배포가 됐다 ================");
        LiveMatchResponse event = matchService.getLiveEvent(matchId);

        return new ResponseEntity<>(ResponseDTO.success(event), HttpStatus.OK);
    }

    @Scheduled(fixedRate = 30000)
    public void getLiveEventSchedule(){
        List<LiveMatchResponse> events = matchService.getLiveEvents();
        events.stream().forEach( event -> {
            messagingTemplate.convertAndSend("/topic/liveMatch/" + event.getRapidId(), event);
        });
    }

    @PostMapping("/match")
    public ResponseEntity<ResponseDTO> saveMatch(){
        matchService.saveMatchList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/statistic")
    public ResponseEntity<ResponseDTO> saveStatistic(){
        matchService.saveStatisticList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
