package com.tennisfolio.Tennisfolio.statistic.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.statistic.application.StatisticSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
public class StatisticController {

    private final StatisticSyncService statisticSyncService;

    public StatisticController(StatisticSyncService statisticSyncService) {
        this.statisticSyncService = statisticSyncService;
    }

    @PostMapping("/")
    public ResponseEntity<ResponseDTO> saveStatistic(){
        statisticSyncService.saveStatisticList();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}