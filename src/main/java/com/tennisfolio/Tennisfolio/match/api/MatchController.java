package com.tennisfolio.Tennisfolio.match.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.match.application.LiveMatchService;
import com.tennisfolio.Tennisfolio.match.application.MatchSyncService;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchSummaryResponse;
import com.tennisfolio.Tennisfolio.statistic.application.StatisticSyncService;
import com.tennisfolio.Tennisfolio.match.dto.LiveMatchResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(
    name = "batch.enabled",
    havingValue = "true"
)
public class MatchController {
    private final LiveMatchService liveMatchService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchSyncService matchSyncService;

    public MatchController(LiveMatchService liveMatchService, SimpMessagingTemplate messagingTemplate, MatchSyncService matchSyncService){
        this.liveMatchService = liveMatchService;
        this.messagingTemplate = messagingTemplate;
        this.matchSyncService = matchSyncService;
    }
    @GetMapping("/atp/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getATPLiveEvents(){
        List<LiveMatchResponse> events = liveMatchService.getATPLiveEventsByRedis();
        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }
    @Scheduled(cron="5,35 * * * * *", zone= "Asia/Seoul")
    public void getATPLiveEventsSchedule(){

        List<LiveMatchResponse> events = liveMatchService.getATPLiveEventsByRedis();
        messagingTemplate.convertAndSend("/topic/atp/liveMatches", events);
    }

    @GetMapping("/wta/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getWTALiveEvents(){
        List<LiveMatchResponse> events = liveMatchService.getWTALiveEventsByRedis();
        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }

    @Scheduled(cron="5,35 * * * * *", zone= "Asia/Seoul")
    public void getWTALiveEventsSchedule(){
        List<LiveMatchResponse> events = liveMatchService.getWTALiveEventsByRedis();
        messagingTemplate.convertAndSend("/topic/wta/liveMatches", events);
    }

    @GetMapping("/etc/liveEvents")
    public ResponseEntity<ResponseDTO<List<LiveMatchResponse>>> getEtcLiveEvents(){
        List<LiveMatchResponse> events = liveMatchService.getEtcLiveEventsByRedis();
        return new ResponseEntity<>(ResponseDTO.success(events), HttpStatus.OK);
    }

    @Scheduled(cron="5,35 * * * * *", zone= "Asia/Seoul")
    public void getEtcLiveEventsSchedule(){
        List<LiveMatchResponse> events = liveMatchService.getEtcLiveEventsByRedis();
        messagingTemplate.convertAndSend("/topic/etc/liveMatches", events);
    }

    @GetMapping("/liveEvents/{matchId}")
    public ResponseEntity<ResponseDTO<LiveMatchResponse>> getLiveEvent(@PathVariable("matchId") String matchId){
        LiveMatchResponse event = liveMatchService.getLiveEventByRedis(matchId);

        return new ResponseEntity<>(ResponseDTO.success(event), HttpStatus.OK);
    }

    @Scheduled(cron="5,35 * * * * *", zone= "Asia/Seoul")
    public void getLiveEventSchedule(){
        List<LiveMatchResponse> events = liveMatchService.getAllLiveEventsByRedis();
        events.stream().forEach( event -> {
            messagingTemplate.convertAndSend("/topic/liveMatch/" + event.getRapidId(), event);
        });
    }

    @PostMapping("/match")
    public ResponseEntity<ResponseDTO> saveMatch(){
        matchSyncService.saveMatchList();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/asyncMatch")
    public ResponseEntity<ResponseDTO> asyncSaveMatch(){
        matchSyncService.loadMatchesByRound();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/event")
    public ResponseEntity<ResponseDTO> saveEventSchedule(){

        matchSyncService.saveEventSchedule();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/liveEvents/summary")
    public ResponseEntity<ResponseDTO> getSummaryLiveEvents(){
        List<LiveMatchSummaryResponse> res = liveMatchService.getLiveEventsSummary();
        return new ResponseEntity<>(ResponseDTO.success(res), HttpStatus.OK);
    }

}
