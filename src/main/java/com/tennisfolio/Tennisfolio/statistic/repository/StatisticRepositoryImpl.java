package com.tennisfolio.Tennisfolio.statistic.repository;

import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.infrastructure.repository.StatisticJpaRepository;
import com.tennisfolio.Tennisfolio.infrastructure.saver.BufferedBatchSaver;
import com.tennisfolio.Tennisfolio.match.domain.Match;
import com.tennisfolio.Tennisfolio.match.repository.MatchEntity;
import com.tennisfolio.Tennisfolio.statistic.domain.Statistic;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Repository
public class StatisticRepositoryImpl implements StatisticRepository{
    private final StatisticJpaRepository statisticJpaRepository;
    private final BufferedBatchSaver<StatisticEntity> bufferedBatchSaver;

    public StatisticRepositoryImpl(StatisticJpaRepository statisticJpaRepository, TransactionTemplate transactionTemplate) {
        this.statisticJpaRepository = statisticJpaRepository;
        this.bufferedBatchSaver = new BufferedBatchSaver<>(statisticJpaRepository, 100,transactionTemplate);
    }

    @Override
    public List<Statistic> findAll() {
        return statisticJpaRepository.findAll().stream().map(StatisticEntity::toModel).toList();
    }

    @Override
    public List<Statistic> findByMatch(Match match) {
        return statisticJpaRepository.findByMatchEntity(MatchEntity.fromModel(match)).stream().map(StatisticEntity::toModel).toList();
    }


    @Override
    public List<Statistic> collect(Statistic statistic) {

        return bufferedBatchSaver
                .collect(StatisticEntity.fromModel(statistic))
                .stream()
                .map(StatisticEntity::toModel)
                .toList();

    }

    @Override
    public List<Statistic> collect(List<Statistic> statistics) {
        List<StatisticEntity> entities = statistics.stream().map(StatisticEntity::fromModel).toList();

        return bufferedBatchSaver.collect(entities).stream().map(StatisticEntity::toModel).toList();

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
    public void save(Statistic statistic) {
        statisticJpaRepository.save(StatisticEntity.fromModel(statistic));
    }
}
