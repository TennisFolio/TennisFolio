package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentRepository;
import com.tennisfolio.Tennisfolio.category.domain.Category;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeTournamentRepository implements TournamentRepository {

    private final Map<Long, Tournament> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);
    private final int batchSize = 3;
    private final List<Tournament> batch = Collections.synchronizedList(new ArrayList<>());


    @Override
    public Tournament save(Tournament tournament) {
        Tournament saved = tournament;
        if(tournament.getTournamentId() == null || tournament.getTournamentId() == 0L){
            saved = Tournament.builder()
                    .tournamentId(seq.getAndIncrement())
                    .category(tournament.getCategory())
                    .rapidTournamentId(tournament.getRapidTournamentId())
                    .tournamentName(tournament.getTournamentName())
                    .matchType(tournament.getMatchType())
                    .city(tournament.getCity())
                    .groundType(tournament.getGroundType())
                    .logo(tournament.getLogo())
                    .mostTitles(tournament.getMostTitles())
                    .mostTitlePlayer(tournament.getMostTitlePlayer())
                    .titleHolder(tournament.getTitleHolder())
                    .points(tournament.getPoints())
                    .build();
        }
        data.put(saved.getTournamentId(), saved);
        return saved;
    }

    @Override
    public List<Tournament> saveAll(List<Tournament> tournaments) {

        return tournaments.stream().map(this::save).toList();
    }

    @Override
    public List<Tournament> findAll() {
        return data.values().stream()
                .map(this::copyWithoutAssociations)
                .toList();
    }

    private Tournament copyWithoutAssociations(Tournament src){
        return Tournament.builder()
                .tournamentId(src.getTournamentId())
                .tournamentName(src.getTournamentName())
                .rapidTournamentId(src.getRapidTournamentId())
                .matchType(src.getMatchType())
                .city(src.getCity())
                .groundType(src.getGroundType())
                .logo(src.getLogo())
                .mostTitles(src.getMostTitles())
                .points(src.getPoints())
                .build();
    }

    @Override
    public List<Tournament> findAllWithPlayers() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Set<String> findAllRapidIds() {
        return data.values().stream()
                .map(Tournament::getRapidTournamentId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Tournament> findByRapidTournamentIds(List<String> ids) {
        return data.values().stream()
                .filter(tournament -> ids.contains(tournament.getRapidTournamentId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tournament> findByCategoryIn(List<Category> ids) {
        return data.values().stream()
                .filter(tournament -> ids.contains(tournament.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Tournament> findByRapidTournamentId(String rapidId) {
        return data.values().stream()
                .filter(tournament -> rapidId.equals(tournament.getRapidTournamentId()))
                .map(tournament -> Tournament.builder()
                        .tournamentId(tournament.getTournamentId())
                        .tournamentName(tournament.getTournamentName())
                        .rapidTournamentId(tournament.getRapidTournamentId())
                        .city(tournament.getCity())
                        .points(tournament.getPoints())
                        .logo(tournament.getLogo())
                        .mostTitles(tournament.getMostTitles())
                        .groundType(tournament.getGroundType())
                        .build())
                .findFirst();
    }

    @Override
    public Optional<Tournament> findWithCategoryByRapidTournamentId(String rapidId) {
        return data.values().stream()
                .filter(tournament -> rapidId.equals(tournament.getRapidTournamentId()))
                .findFirst();
    }

    @Override
    public List<Tournament> collect(Tournament tournament) {
        batch.add(tournament);
        return batch;
    }

    @Override
    public List<Tournament> collect(List<Tournament> tournaments) {
        batch.addAll(tournaments);
        return batch;
    }

    @Override
    public boolean flushWhenFull() {
        if(batch.size() >= batchSize){
            saveAll(batch);
            batch.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean flushAll() {
        if(!batch.isEmpty()){
            saveAll(batch);
            batch.clear();
            return true;
        }
        return false;

    }

    @Override
    public void flush() {

    }
}
