package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TournamentRepositoryImpl implements TournamentRepository{
    private final TournamentJpaRepository tournamentJpaRepository;
    private final BufferedBatchSaver<TournamentEntity> bufferedBatchSaver;
    public TournamentRepositoryImpl(TournamentJpaRepository tournamentJpaRepository, TransactionTemplate transactionTemplate) {
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(tournamentJpaRepository, 1000,transactionTemplate);
    }

    @Override
    public TournamentEntity save(TournamentEntity tournament) {
        return tournamentJpaRepository.save(tournament);
    }

    @Override
    public List<TournamentEntity> saveAll(List<TournamentEntity> tournaments) {
        return tournamentJpaRepository.saveAll(tournaments);
    }

    @Override
    public List<TournamentEntity> findAll() {
        return tournamentJpaRepository.findAll();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return tournamentJpaRepository.findAllRapidTournamentIds();
    }

    @Override
    public List<TournamentEntity> findByRapidTournamentIds(List<String> ids) {
        return tournamentJpaRepository.findByRapidTournamentIds(ids);
    }

    @Override
    public Optional<TournamentEntity> findByRapidTournamentId(String rapidId) {
        return tournamentJpaRepository.findByRapidTournamentId(rapidId);
    }
    @Override
    public List<TournamentEntity> collect(TournamentEntity tournament){
        return bufferedBatchSaver.collect(tournament);
    }
    @Override
    public List<TournamentEntity> collect(List<TournamentEntity> tournaments){
        return bufferedBatchSaver.collect(tournaments);
    }
    @Override
    public boolean flushWhenFull(){
        return bufferedBatchSaver.flushWhenFull();
    }
    @Override
    public boolean flushAll(){
        return bufferedBatchSaver.flushAll();
    }
}
