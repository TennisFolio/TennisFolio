package com.tennisfolio.Tennisfolio.match.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import com.tennisfolio.Tennisfolio.match.application.StatisticSyncService;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final LiveMatchService liveMatchService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchSyncService matchSyncService;
    private final StatisticSyncService statisticSyncService;

    public MatchController(LiveMatchService liveMatchService, SimpMessagingTemplate messagingTemplate, MatchSyncService matchSyncService, StatisticSyncService statisticSyncService){
        this.liveMatchService = liveMatchService;
        this.messagingTemplate = messagingTemplate;
        this.matchSyncService = matchSyncService;
        this.statisticSyncService = statisticSyncService;
    }
    @GetMapping("/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getLiveEvents(){
        List<LiveMatchResponse> events = liveMatchService.getLiveEvents();

        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }
    @Scheduled(fixedRate = 30000)
    public void getLiveEventsSchedule(){
        List<LiveMatchResponse> events = liveMatchService.getLiveEvents();
        messagingTemplate.convertAndSend("/topic/liveMatches", events);
    }

    @GetMapping("/liveEvents/{matchId}")
    public ResponseEntity<ResponseDTO<LiveMatchResponse>> getLiveEvent(@PathVariable("matchId") String matchId){
        LiveMatchResponse event = liveMatchService.getLiveEvent(matchId);

        return new ResponseEntity<>(ResponseDTO.success(event), HttpStatus.OK);
    }

    @Scheduled(fixedRate = 30000)
    public void getLiveEventSchedule(){
        List<LiveMatchResponse> events = liveMatchService.getLiveEvents();
        events.stream().forEach( event -> {
            messagingTemplate.convertAndSend("/topic/liveMatch/" + event.getRapidId(), event);
        });
    }

    @PostMapping("/match")
    public ResponseEntity<ResponseDTO> saveMatch(){
        matchSyncService.saveMatchList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/statistic")
    public ResponseEntity<ResponseDTO> saveStatistic(){
        statisticSyncService.saveStatisticList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
