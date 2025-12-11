package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import com.tennisfolio.Tennisfolio.statistic.repository.StatisticRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeStatisticRepository implements StatisticRepository {
    private final Map<Long, Statistic> data = new ConcurrentHashMap<>();
    private final int batchSize = 3;
    private final List<Statistic> batch = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<Statistic> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public List<Statistic> findByMatch(Match match) {
        return data.values().stream().filter(p -> p.getMatch().equals(match)).toList();
    }

    @Override
    public List<Statistic> findWithMatchAndPlayerByYear(String year) {
        return List.of();
    }

    @Override
    public List<Statistic> collect(Statistic statistic) {
        batch.add(statistic);
        return batch;
    }

    @Override
    public List<Statistic> collect(List<Statistic> statistics) {
        batch.addAll(statistics);
        return batch;
    }

    @Override
    public boolean flushWhenFull() {
        if(batch.size() >= batchSize){
            flushBatch();
            return true;
        }
        return false;
    }

    @Override
    public boolean flushAll() {
       if(batch.size() > 0){
           flushBatch();
           return true;
       }
       return false;
    }

    @Override
    public void save(Statistic statistic) {
        data.put(statistic.getStatId(), statistic);
    }

    @Override
    public void saveAll(List<Statistic> statistics) {

    }

    private void flushBatch() {
        for (var statistic : batch) {
            long statId = statistic.getStatId();
            data.put(statId, statistic); // 중복이면 자동 덮어쓰기
        }
        batch.clear();
    }
}
