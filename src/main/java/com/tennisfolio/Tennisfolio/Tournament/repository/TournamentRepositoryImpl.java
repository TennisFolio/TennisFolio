package com.tennisfolio.Tennisfolio.Tournament.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.category.domain.Category;
import com.tennisfolio.Tennisfolio.category.repository.CategoryEntity;
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
    public Tournament save(Tournament tournament) {

        return tournamentJpaRepository.save(TournamentEntity.fromModel(tournament)).toModel();
    }

    @Override
    public List<Tournament> saveAll(List<Tournament> tournaments) {
        List<TournamentEntity> entities = tournaments.stream().map(TournamentEntity::fromModel).toList();
        return tournamentJpaRepository.saveAll(entities).stream().map(TournamentEntity::toModel).toList();
    }

    @Override
    public List<Tournament> findAll() {
        return tournamentJpaRepository.findAll().stream().map(TournamentEntity::toModelBaseOnly).toList();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return tournamentJpaRepository.findAllRapidTournamentIds();
    }

    @Override
    public List<Tournament> findByRapidTournamentIds(List<String> ids) {
        return tournamentJpaRepository.findByRapidTournamentIds(ids).stream().map(TournamentEntity::toModel).toList();
    }

    @Override
    public List<Tournament> findByCategoryIn(List<Category> categories) {
        List<CategoryEntity> categoryEntities = categories.stream().map(CategoryEntity::fromModel).toList();
        return tournamentJpaRepository.findByCategoryIn(categoryEntities).stream().map(TournamentEntity::toModelBaseOnly).toList();
    }

    @Override
    public Optional<Tournament> findByRapidTournamentId(String rapidId) {
        return tournamentJpaRepository.findByRapidTournamentId(rapidId).map(TournamentEntity::toModelBaseOnly);
    }
    @Override
    public List<Tournament> collect(Tournament tournament){
        return bufferedBatchSaver.collect(TournamentEntity.fromModel(tournament))
                .stream()
                .map(TournamentEntity::toModel)
                .toList();
    }
    @Override
    public List<Tournament> collect(List<Tournament> tournaments){
        List<TournamentEntity> entities = tournaments.stream().map(TournamentEntity::fromModel).toList();
        return bufferedBatchSaver.collect(entities).stream().map(TournamentEntity::toModel).toList();
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
