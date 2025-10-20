package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchRepository;
import com.tennisfolio.Tennisfolio.season.domain.Season;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeMatchRepository implements MatchRepository {
    private final Map<Long, Match> data = new ConcurrentHashMap<>();
    private final int batchSize = 3;
    private final AtomicLong seq = new AtomicLong(1);
    private final List<Match> batch = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Optional<Match> findByRapidMatchId(String rapidMatchId) {
        return data.values().stream().filter(p -> rapidMatchId.equals(p.getRapidMatchId())).findFirst();
    }

    @Override
    public Match save(Match match) {
        return data.put(match.getMatchId(), match);
    }

    @Override
    public List<Match> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return data.values().stream().map(Match::getRapidMatchId).collect(Collectors.toSet());
    }

    @Override
    public List<Match> collect(Match match) {
        batch.add(match);
        return batch;
    }

    @Override
    public List<Match> collect(List<Match> matches) {
        batch.addAll(matches);
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

    @Override
    public void flush() {

    }

    private void flushBatch() {
        for (var match : batch) {
            long matchId = match.getMatchId();
            data.put(matchId, match); // 중복이면 자동 덮어쓰기
        }
        batch.clear();
    }
}
