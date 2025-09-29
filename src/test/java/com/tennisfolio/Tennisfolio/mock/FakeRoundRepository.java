package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.round.domain.Round;
import com.tennisfolio.Tennisfolio.round.repository.RoundRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeRoundRepository implements RoundRepository {

    private final Map<Long, Round> data = new ConcurrentHashMap<>();
    private final int batchSize = 3;
    private final List<Round> batch = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<Round> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public Set<Pair<Season, String>> findAllSeasonRoundPairs() {
        return data.values().stream().map(pair -> Pair.of(pair.getSeason(), pair.getSlug()))
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Round> findBySeasonAndRoundAndSlug(Season season, Long round, String slug) {
        return data.values()
                .stream()
                .filter(p -> p.getSeason().getSeasonId().equals(season.getSeasonId()) &&
                p.getRound().equals(round) &&
                p.getSlug().equals(slug)).findFirst();
    }

    @Override
    public List<Round> collect(Round round) {
        batch.add(round);
        return batch;
    }

    @Override
    public List<Round> collect(List<Round> rounds) {
        batch.addAll(rounds);
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
        if(!batch.isEmpty()){
            flushBatch();
            return true;
        }
        return false;
    }

    private void flushBatch() {
        for (var round : batch) {
            long roundId = round.getRoundId();
            data.put(roundId, round); // 중복이면 자동 덮어쓰기
        }
        batch.clear();
    }
}
