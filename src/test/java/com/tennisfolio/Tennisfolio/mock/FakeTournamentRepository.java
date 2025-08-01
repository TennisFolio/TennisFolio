package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;

import java.util.*;

public class FakeTournamentRepository implements TournamentRepository {

    private final List<Tournament> data = Collections.synchronizedList(new ArrayList<>());


    @Override
    public Tournament save(Tournament tournament) {
        data.add(tournament);
        return tournament;
    }

    @Override
    public List<Tournament> saveAll(List<Tournament> tournaments) {
        return List.of();
    }

    @Override
    public List<Tournament> findAll() {
        return List.of();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return Set.of();
    }

    @Override
    public List<Tournament> findByRapidTournamentIds(List<String> ids) {
        return List.of();
    }

    @Override
    public Optional<Tournament> findByRapidTournamentId(String rapidId) {
        return null;
    }

    @Override
    public List<Tournament> collect(Tournament tournament) {
        return List.of();
    }

    @Override
    public List<Tournament> collect(List<Tournament> tournaments) {
        return List.of();
    }

    @Override
    public boolean flushWhenFull() {
        return false;
    }

    @Override
    public boolean flushAll() {
        return false;
    }
}
