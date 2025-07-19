package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.infrastructure.repository.TournamentJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TournamentRepositoryImpl implements TournamentRepository{
    private final TournamentJpaRepository tournamentJpaRepository;
    private final BufferedBatchSaver<Tournament> bufferedBatchSaver;
    public TournamentRepositoryImpl(TournamentJpaRepository tournamentJpaRepository) {
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(tournamentJpaRepository, 1000);
    }

    @Override
    public Tournament save(Tournament tournament) {
        return tournamentJpaRepository.save(tournament);
    }

    @Override
    public List<Tournament> saveAll(List<Tournament> tournaments) {
        return tournamentJpaRepository.saveAll(tournaments);
    }

    @Override
    public List<Tournament> findByRapidTournamentIds(List<String> ids) {
        return tournamentJpaRepository.findByRapidTournamentIds(ids);
    }

    @Override
    public Optional<Tournament> findByRapidTournamentId(String rapidId) {
        return tournamentJpaRepository.findByRapidTournamentId(rapidId);
    }

    @Override
    public List<Tournament> bufferedSave(Tournament tournament) {
        return bufferedBatchSaver.collect(tournament);
    }

    @Override
    public List<Tournament> bufferedSaveAll(List<Tournament> tournaments) {
        return bufferedBatchSaver.collect(tournaments);
    }

    @Override
    public List<Tournament> flush() {
        return bufferedBatchSaver.flush();
    }
}
