package com.tennisfolio.Tennisfolio.statistic.application;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiWorker;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RetryExecutor;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.StrategyApiTemplate;
import com.tennisfolio.Tennisfolio.infrastructure.api.statistic.eventStatistics.EventsStatisticsDTO;
import com.tennisfolio.Tennisfolio.infrastructure.worker.GenericBatchWorker;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class StatisticSyncService {

    private final ApiWorker apiWorker;
    private final MatchRepository matchRepository;
    private final StatisticRepository statisticRepository;
    private final RetryExecutor retryExecutor;
    private final GenericBatchWorker<Statistic> statisticBatchWorker;
    public StatisticSyncService(ApiWorker apiWorker, MatchRepository matchRepository, StatisticRepository statisticRepository, RetryExecutor retryExecutor, GenericBatchWorker<Statistic> statisticBatchWorker) {
        this.apiWorker = apiWorker;
        this.matchRepository = matchRepository;
        this.statisticRepository = statisticRepository;
        this.retryExecutor = retryExecutor;
        this.statisticBatchWorker = statisticBatchWorker;
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
                        if(statistics == null) return;

                        statistics.stream().forEach(p -> p.updateMatch(match));

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

    public void loadStatisticsByMatch(String year){
        ExecutorService producerExecutor = Executors.newFixedThreadPool(5);

        Set<String> existingRapidMatchId = statisticRepository.findWithMatchAndPlayerByYear(year)
                .stream()
                .map(p -> p.getMatch().getRapidMatchId())
                .collect(Collectors.toSet());

        List<Match> allMatches = matchRepository.findByYear(year)
                .stream()
                .toList();

        int MAX_PENDING_BEFORE_API = 2000;
        long CAPACITY_CHECK_INTERVAL_MS = 50L;

        List<CompletableFuture<Void>> futures = allMatches.stream()
                .map(match -> CompletableFuture.runAsync(() -> {
                    try{
                        String rapidMatchId = match.getRapidMatchId();

                        if(existingRapidMatchId.contains(rapidMatchId)) return;

                        statisticBatchWorker.awaitCapacity(MAX_PENDING_BEFORE_API, CAPACITY_CHECK_INTERVAL_MS);

                        log.info("Producer Thread: {} - match={}",
                                Thread.currentThread().getName(),
                                match.getRapidMatchId());

                        List<Statistic> statistics = retryExecutor.callWithRetry(() ->
                                apiWorker.process(
                                        RapidApi.EVENTSTATISTICS,
                                        rapidMatchId
                                ));

                        if(statistics == null) return;

                        List<Statistic> newStatistics = statistics.stream().toList();

                        newStatistics.stream().forEach(p -> p.updateMatch(match));

                        statisticBatchWorker.submit(newStatistics);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }, producerExecutor)).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        producerExecutor.shutdown();
        statisticBatchWorker.shutdownAndAwait(60, TimeUnit.SECONDS);
    }
}
