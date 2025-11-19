package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import com.tennisfolio.Tennisfolio.season.repository.SeasonRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeSeasonRepository implements SeasonRepository {

    private final Map<Long, Season> data = new ConcurrentHashMap<>();
    private final int batchSize = 3;
    private final AtomicLong seq = new AtomicLong(1);
    private final List<Season> batch = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<Season> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public Season save(Season season) {
        data.put(seq.getAndIncrement(), season);
        return season;
    }

    @Override
    public List<Season> collect(Season season) {
        batch.add(season);
        return batch;
    }

    @Override
    public List<Season> collect(List<Season> seasons) {
        batch.addAll(seasons);
        return batch;
    }

    @Override
    public Set<String> findAllRapidIds() {
        return findAll().stream().map(Season::getRapidSeasonId).collect(Collectors.toSet());
    }

    @Override
    public List<Season> findByRapidSeasonIdIn(Set<String> rapidSeasonIds) {
        return data.values()
                .stream()
                .filter(p -> rapidSeasonIds.contains(p.getRapidSeasonId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Season> findByRapidSeasonId(String rapidId) {
        return findAll().stream().filter(season -> rapidId.equals(season.getRapidSeasonId())).findFirst();
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
        if(!batch.isEmpty()){
            flushBatch();
            return true;
        }
        return false;
    }

    @Override
    public void flush() {

    }

    private void flushBatch() {
        for (var season : batch) {
            long seasonId = season.getSeasonId();
            data.put(seasonId, season); // 중복이면 자동 덮어쓰기
        }
        batch.clear();
    }

    public void deleteAll(){
        data.clear();
    }
}
