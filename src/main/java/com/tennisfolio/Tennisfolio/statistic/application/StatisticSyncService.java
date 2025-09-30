package com.tennisfolio.Tennisfolio.statistic.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatisticSyncService {

    private final ApiWorker apiWorker;
    private final MatchRepository matchRepository;
    private final StatisticRepository statisticRepository;
    public StatisticSyncService( ApiWorker apiWorker, MatchRepository matchRepository, StatisticRepository statisticRepository) {
        this.apiWorker = apiWorker;
        this.matchRepository = matchRepository;
        this.statisticRepository = statisticRepository;
    }

    public void saveStatisticList() {
        List<String> failedRapidIds = new ArrayList<>();
        matchRepository.findAll()
                .stream()
                .forEach(match -> {
                    try{
                        String rapidMatchId = match.getRapidMatchId();

                        if(!statisticRepository.findByMatch(match).isEmpty()){
                            return;
                        }
                        List<Statistic> statistics = apiWorker.process(RapidApi.EVENTSTATISTICS, rapidMatchId);
                        statisticRepository.collect(statistics);
                        statisticRepository.flushWhenFull();
                    }catch(Exception e){
                        e.printStackTrace();
                        failedRapidIds.add(match.getRapidMatchId());
                    }
                });

        System.out.println("failedRapidIds: " + failedRapidIds);

        statisticRepository.flushAll();

    }
}
