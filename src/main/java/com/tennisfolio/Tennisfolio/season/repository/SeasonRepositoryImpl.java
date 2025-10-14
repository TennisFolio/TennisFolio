package com.tennisfolio.Tennisfolio.season.repository;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.SeasonJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.season.domain.Season;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class SeasonRepositoryImpl implements SeasonRepository{
    private final SeasonJpaRepository seasonJpaRepository;
    private final BufferedBatchSaver<SeasonEntity> bufferedBatchSaver;

    public SeasonRepositoryImpl(SeasonJpaRepository seasonJpaRepository, TransactionTemplate transactionTemplate) {
        this.seasonJpaRepository = seasonJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(seasonJpaRepository, 1000, transactionTemplate);
    }

    @Override
    public List<Season> findAll() {
        return seasonJpaRepository.findAll().stream().map(SeasonEntity::toModel).toList();
    }

    @Override
    public Season save(Season season) {
        return seasonJpaRepository.save(SeasonEntity.fromModel(season)).toModel();
    }

    @Override
    public List<Season> collect(Season season) {
        return bufferedBatchSaver.collect(SeasonEntity.fromModel(season))
                .stream()
                .map(SeasonEntity::toModelBaseOnly)
                .toList();
    }

    @Override
    public List<Season> collect(List<Season> seasons) {
        List<SeasonEntity> entities = seasons.stream().map(SeasonEntity::fromModel).toList();
        return bufferedBatchSaver.collect(entities).stream().map(SeasonEntity::toModelBaseOnly).toList();
    }

    @Override
    public Set<String> findAllRapidIds() {
        return seasonJpaRepository.findAllRapidSeasonIds();
    }

    @Override
    public Optional<Season> findByRapidSeasonId(String rapidId) {
        return seasonJpaRepository.findByRapidSeasonId(rapidId).map(SeasonEntity::toModel);
    }

    @Override
    public boolean flushWhenFull() {
        return bufferedBatchSaver.flushWhenFull();
    }

    @Override
    public boolean flushAll() {
        return bufferedBatchSaver.flushAll();
    }

    @Override
    public void flush() {
        seasonJpaRepository.flush();
    }
}
